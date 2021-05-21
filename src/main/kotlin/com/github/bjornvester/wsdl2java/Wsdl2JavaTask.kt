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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.JavaCompile
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

    @get:Input
    @Optional
    val encoding = objects.property(String::class.java).convention(getWsdl2JavaExtension().encoding)

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

        val workerExecutor = workerExecutor.processIsolation {
            /*
            All gradle worker processes have Xerces2 on the classpath.
            This version of Xerces does not support checking for external file access (even if not used).
            This causes it to log a whole bunch of stack traces on the form:
            -- Property "http://javax.xml.XMLConstants/property/accessExternalSchema" is not supported by used JAXP implementation.
            To avoid this, we fork the worker API to a separate process where we can set system properties to select which implementation of a SAXParser to use.
            The JDK comes with an internal implementation of a SAXParser, also based on Xerces, but supports the properties to control external file access.
            */
            forkOptions.systemProperties = mapOf(
                "javax.xml.parsers.DocumentBuilderFactory" to "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                "javax.xml.parsers.SAXParserFactory" to "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
                "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema" to "org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
                "javax.xml.accessExternalSchema" to "all"
            )

            if (logger.isDebugEnabled) {
                // This adds debugging information on the XJC method used to find and load services (plugins)
                forkOptions.systemProperties["com.sun.tools.xjc.Options.findServices"] = ""
            }

            // Set encoding (work-around for https://github.com/gradle/gradle/issues/13843)
            forkOptions.environment("LANG", System.getenv("LANG") ?: "C.UTF-8")

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
            "-xjc-disableXmlSecurity",
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

        if (encoding.isPresent) {
            defaultArgs.addAll(listOf("-encoding", encoding.get()))
        } else {
            // JavaCompile.options.encoding is nullable so task.map{} cannot be effectively used
            // https://github.com/gradle/gradle/issues/12388
            val javaCompile = project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile::class.java).get()
            javaCompile.options.encoding?.let {
                defaultArgs.addAll(listOf("-encoding", it))
            }
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
                "-encoding" to "Configured through the 'encoding' property",
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
