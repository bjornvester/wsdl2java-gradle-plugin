package com.github.bjornvester.wsdl2java

import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_EXTENSION_NAME
import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.GENERATED_STYLE_DEFAULT
import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.GENERATED_STYLE_JAKARTA
import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.GENERATED_STYLE_JDK8
import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.GENERATED_STYLE_JDK9
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject


@CacheableTask
abstract class Wsdl2JavaTask @Inject constructor(
        private val workerExecutor: WorkerExecutor,
        private val fileOperations: FileOperations,
        objects: ObjectFactory
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val wsdlInputDir = objects.directoryProperty().convention(getWsdl2JavaExtension().wsdlDir)

    @get:Input
    val includes = objects.listProperty(String::class.java).convention(getWsdl2JavaExtension().includes)

    @get:Input
    val includesWithOptions = objects.mapProperty(String::class.java, List::class.java).convention(getWsdl2JavaExtension().includesWithOptions)

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    val bindingFile = objects.fileProperty().convention(getWsdl2JavaExtension().bindingFile)

    @get:Input
    @Optional
    val options = objects.listProperty(String::class.java).convention(getWsdl2JavaExtension().options)

    @get:Input
    @Optional
    val verbose = objects.property(Boolean::class.java).convention(getWsdl2JavaExtension().verbose)

    @get:Input
    val suppressGeneratedDate = objects.property(Boolean::class.java).convention(getWsdl2JavaExtension().suppressGeneratedDate)

    @get:Input
    @Optional
    val markGenerated = objects.property(Boolean::class.java).convention(getWsdl2JavaExtension().markGenerated)

    @get:Input
    @Optional
    val generatedStyle = objects.property(String::class.java).convention(getWsdl2JavaExtension().generatedStyle)

    @get:Input
    @Optional
    val packageName = objects.property(String::class.java).convention(getWsdl2JavaExtension().packageName)

    @get:Classpath
    val wsdl2JavaConfiguration = objects.fileCollection()

    @get:Classpath
    val xjcPluginsConfiguration = objects.fileCollection()

    @get:OutputDirectory
    val sourcesOutputDir: DirectoryProperty = objects.directoryProperty().convention(getWsdl2JavaExtension().generatedSourceDir)

    @get:Nested
    val javaLauncher: Property<JavaLauncher> = objects.property(JavaLauncher::class.java)

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
            forkOptions.executable = javaLauncher.get().executablePath.asFile.absolutePath;

            /*
            All gradle worker processes have Xerces2 on the classpath.
            This version of Xerces does not support checking for external file access (even if not used).
            This causes it to log a bunch of stack traces on the form:
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

            // Set encoding (work-around for https://github.com/gradle/gradle/issues/
            // Might be fixed in Gradle 8.3 (unreleased at the time of this writing)
            forkOptions.environment("LANG", System.getenv("LANG") ?: "C.UTF-8")

            classpath
                    .from(wsdl2JavaConfiguration)
                    .from(xjcPluginsConfiguration)
        }

        val defaultArgs = buildDefaultArguments()
        val wsdlToArgs = mutableMapOf<String, List<String>>()

        if (includesWithOptions.isPresent && includesWithOptions.get().isNotEmpty()) {
            includesWithOptions.get().forEach { (includePattern, includeOptions) ->
                addWsdlToArgs(listOf(includePattern), defaultArgs + includeOptions as List<String>, wsdlToArgs)
            }
        } else {
            addWsdlToArgs(includes.get(), defaultArgs, wsdlToArgs)
        }

        // Note that we don't run the CXF tool on each WSDL file as the build might be configured with parallel execution
        // This could be a problem if multiple WSDLs references the same schemas as CXF might try and write to the same files
        workerExecutor.submit(Wsdl2JavaWorker::class.java) {
            this.wsdlToArgs = wsdlToArgs
            outputDir = sourcesOutputDir.get()
            generatedStyle = this@Wsdl2JavaTask.generatedStyle.get()
            removeDateFromGeneratedAnnotation = (markGenerated.get() && suppressGeneratedDate.get())
        }
    }

    private fun addWsdlToArgs(
            includePattern: List<String>?,
            defaultArgs: List<String>,
            wsdlToArgs: MutableMap<String, List<String>>
    ) {
        wsdlInputDir
                .asFileTree
                .matching { if (includePattern != null) include(includePattern) }
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

        if (markGenerated.get()) {
            defaultArgs.add("-mark-generated")
        }

        if (packageName.isPresent && packageName.get().isNotBlank()) {
            defaultArgs.addAll(listOf("-p", packageName.get()))
        }

        // Add the verbose parameter if explicitly configured to true, or if not set but info logging is enabled
        if (verbose.getOrElse(false) || (!verbose.isPresent && logger.isInfoEnabled)) {
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
        val supportedGeneratedStyleValues = listOf(GENERATED_STYLE_DEFAULT, GENERATED_STYLE_JDK8, GENERATED_STYLE_JDK9, GENERATED_STYLE_JAKARTA)
        if (generatedStyle.get() !in supportedGeneratedStyleValues) {
            throw GradleException("The property 'markGenerated' had an invalid value '${markGenerated.get()}'. Supported values are: $supportedGeneratedStyleValues")
        }

        if (options.isPresent || includesWithOptions.isPresent) {
            val prohibitedOptions = mapOf(
                    "-verbose" to "Configured through the 'verbose' property",
                    "-d" to "Configured through the 'generatedSourceDir' property",
                    "-p" to "Configured through the 'packageName' property",
                    "-suppress-generated-date" to "Configured through the 'suppressGeneratedDate' property",
                    "-mark-generated" to "Configured through the 'markGenerated' property",
                    "-autoNameResolution" to "Configured automatically and cannot currently be overridden"
            )

            // Note that we allow specifying binding file(s) through the -b parameter, as we otherwise can't configure individual bindings pr. wsdl

            (options.getOrElse(emptyList()) + includesWithOptions.getOrElse(emptyMap()).values).forEach { option ->
                if (prohibitedOptions.containsKey(option)) {
                    throw GradleException("The option '$option' is not allowed in this plugin. Reason: ${prohibitedOptions[option]}")
                }
            }
        }
    }

    private fun getWsdl2JavaExtension() = project.extensions.getByName(WSDL2JAVA_EXTENSION_NAME) as Wsdl2JavaPluginExtension
}
