package com.github.bjornvester.wsdl2java

import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_CONFIGURATION_NAME
import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    val wsdlFiles = objects.fileCollection().from(getWsdl2JavaExtension().wsdlFiles)

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    val bindingFile= objects.fileProperty().convention(getWsdl2JavaExtension().bindingFile)

    @get:Input
    @Optional
    val options = objects.listProperty(String::class.java).convention(getWsdl2JavaExtension().options)

    @get:Input
    val verbose = objects.property(Boolean::class.java).convention(getWsdl2JavaExtension().verbose)

    @get:Input
    val suppressGeneratedDate = objects.property(Boolean::class.java).convention(getWsdl2JavaExtension().suppressGeneratedDate)

    @get:OutputDirectory
    val sourcesOutputDir: DirectoryProperty = objects.directoryProperty().convention(getWsdl2JavaExtension().generatedSourceDir)

    init {
        group = BasePlugin.BUILD_GROUP
        description = "Generates Java classes from WSDL files."
        dependsOn(project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME)) // TODO: Can't remember why this was added. Figure out why, and if so, add a comment about it
    }

    @TaskAction
    fun doCodeGeneration() {
        validateOptions()

        fileOperations.delete(sourcesOutputDir)
        fileOperations.mkdir(sourcesOutputDir)

        val workerExecutor = workerExecutor.classLoaderIsolation {
            classpath.from(project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME).get().resolve())
        }

        val defaultArgs = buildDefaultArguments()

        wsdlFiles.forEach { wsdlFile ->
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

            workerExecutor.submit(Wsdl2JavaWorker::class.java) {
                args = computedArgs.toTypedArray()
            }
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
        if (options.isPresent) {
            val prohibitedOptions = mapOf(
                "-verbose" to "Configured through the 'verbose' property",
                "-d" to "Configured through the 'generatedSourceDir' property",
                "-b" to "Configured through the 'bindingFile' property",
                "-suppress-generated-date" to "Configured through the 'suppressGeneratedDate' property",
                "-autoNameResolution" to "Configured automatically and cannot currently be overridden"
            )

            options.get().forEach { option ->
                if (prohibitedOptions.containsKey(option)) {
                    throw GradleException("the option '$option' is not allowed in this plugin. Reason: ${prohibitedOptions[option]}")
                }
            }
        }
    }

    private fun getWsdl2JavaExtension() = project.extensions.getByName(WSDL2JAVA_EXTENSION_NAME) as Wsdl2JavaPluginExtension
}
