package com.github.bjornvester.wsdl2java

import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_CONFIGURATION_NAME
import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_EXTENSION_NAME
import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.XJC_PLUGINS_CONFIGURATION_NAME
import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.MARK_GENERATED_NO
import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.MARK_GENERATED_YES_JDK8
import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.MARK_GENERATED_YES_JDK9
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
open class Wsdl2JavaTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
    private val fileOperations: FileOperations,
    objects: ObjectFactory
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val wsdlInputDir = objects.directoryProperty().convention(getWsdl2JavaExtension().wsdlDir)

    @get:Input
    val includes = objects.listProperty(String::class.java).convention(getWsdl2JavaExtension().includes)

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    val bindingFile = objects.fileProperty().convention(getWsdl2JavaExtension().bindingFile)

    @get:Input
    @Optional
    val options = objects.listProperty(String::class.java).convention(getWsdl2JavaExtension().options)

    @get:Input
    val verbose = objects.property(Boolean::class.java).convention(getWsdl2JavaExtension().verbose)

    @get:Input
    val suppressGeneratedDate = objects.property(Boolean::class.java).convention(getWsdl2JavaExtension().suppressGeneratedDate)

    @get:Input
    @Optional
    val markGenerated = objects.property(String::class.java).convention(getWsdl2JavaExtension().markGenerated)

    @get:Classpath
    val wsdl2JavaConfiguration = project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME)

    @get:Classpath
    val xjcPluginsConfiguration: NamedDomainObjectProvider<Configuration> = project.configurations.named(XJC_PLUGINS_CONFIGURATION_NAME)

    @get:OutputDirectory
    val sourcesOutputDir: DirectoryProperty = objects.directoryProperty().convention(getWsdl2JavaExtension().generatedSourceDir)

    init {
        group = BasePlugin.BUILD_GROUP
        description = "Generates Java classes from WSDL files."
    }

    @TaskAction
    fun doCodeGeneration() {
        validateOptions()

        fileOperations.delete(sourcesOutputDir)
        fileOperations.mkdir(sourcesOutputDir)

        val workerExecutor = workerExecutor.classLoaderIsolation {
            classpath
                .from(wsdl2JavaConfiguration)
                .from(xjcPluginsConfiguration)
        }

        val defaultArgs = buildDefaultArguments()
        val wsdlToArgs = mutableMapOf<String, List<String>>()

        wsdlInputDir
            .asFileTree
            .matching { include(this@Wsdl2JavaTask.includes.get()) }
            .forEach { wsdlFile ->
                val computedArgs = mutableListOf<String>()
                computedArgs.addAll(defaultArgs)

                if (!computedArgs.contains("-wsdlLocation")) {
                    computedArgs.addAll(
                        listOf(
                            "-wsdlLocation",
                            wsdlFile.relativeTo(wsdlInputDir.asFile.get()).invariantSeparatorsPath
                        )
                    )
                }

                computedArgs.add(wsdlFile.path)
                wsdlToArgs[wsdlFile.path] = computedArgs
            }

        // Note that we don't run the CXF tool on each WSDL file as the build might be configured with parallel execution
        // This could be a problem if multiple WSDLs references the same schemas as CXF might try and write to the same files
        workerExecutor.submit(Wsdl2JavaWorker::class.java) {
            this.wsdlToArgs = wsdlToArgs
            outputDir = sourcesOutputDir.get()
            switchGeneratedAnnotation = (markGenerated.get() == MARK_GENERATED_YES_JDK9)
            removeDateFromGeneratedAnnotation =
                (markGenerated.get() in listOf(
                    MARK_GENERATED_YES_JDK8,
                    MARK_GENERATED_YES_JDK9
                )) && suppressGeneratedDate.get()
        }
    }

    private fun buildDefaultArguments(): MutableList<String> {
        val defaultArgs = mutableListOf(
            "-autoNameResolution",
            "-d",
            sourcesOutputDir.get().toString()
        )

        if (suppressGeneratedDate.get()) {
            defaultArgs.add("-suppress-generated-date")
        }

        if (markGenerated.get() in listOf(MARK_GENERATED_YES_JDK8, MARK_GENERATED_YES_JDK9)) {
            defaultArgs.add("-mark-generated")
        }

        if (verbose.get()) {
            defaultArgs.add("-verbose")
        }

        if (bindingFile.isPresent) {
            defaultArgs.addAll(
                listOf(
                    "-b",
                    bindingFile.get().asFile.absolutePath
                )
            )
        }

        if (options.isPresent) {
            defaultArgs.addAll(options.get())
        }
        return defaultArgs
    }

    private fun validateOptions() {
        val supportedMarkGeneratedValues = listOf(MARK_GENERATED_NO, MARK_GENERATED_YES_JDK8, MARK_GENERATED_YES_JDK9)
        if (markGenerated.get() !in supportedMarkGeneratedValues) {
            throw GradleException("The property 'markGenerated' had an invalid value '${markGenerated.get()}'. Supported values are: $supportedMarkGeneratedValues")
        }

        if (options.isPresent) {
            val prohibitedOptions = mapOf(
                "-verbose" to "Configured through the 'verbose' property",
                "-d" to "Configured through the 'generatedSourceDir' property",
                "-b" to "Configured through the 'bindingFile' property",
                "-suppress-generated-date" to "Configured through the 'suppressGeneratedDate' property",
                "-mark-generated" to "Configured through the 'markGenerated' property",
                "-autoNameResolution" to "Configured automatically and cannot currently be overridden"
            )

            options.get().forEach { option ->
                if (prohibitedOptions.containsKey(option)) {
                    throw GradleException("The option '$option' is not allowed in this plugin. Reason: ${prohibitedOptions[option]}")
                }
            }
        }
    }

    private fun getWsdl2JavaExtension() = project.extensions.getByName(WSDL2JAVA_EXTENSION_NAME) as Wsdl2JavaPluginExtension
}
