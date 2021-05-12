package com.github.bjornvester.wsdl2java

import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_CONFIGURATION_NAME
import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.net.URLClassLoader

@CacheableTask
open class Wsdl2JavaTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var wsdlInputDir: DirectoryProperty = getWsdl2JavaExtension().wsdlDir

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    val wsdlFiles: ConfigurableFileCollection = getWsdl2JavaExtension().wsdlFiles

    @get:Input
    @Optional
    val bindingFile: Property<String> = getWsdl2JavaExtension().bindingFile

    @get:OutputDirectory
    val sourcesOutputDir: DirectoryProperty = getWsdl2JavaExtension().generatedSourceDir

    init {
        group = BasePlugin.BUILD_GROUP
        description = "Generates Java classes from WSDL files."
        dependsOn(project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME))
    }

    @TaskAction
    fun doCodeGeneration() {
        project.delete(sourcesOutputDir)

        val computedWsdlFiles = when {
            !wsdlFiles.isEmpty -> wsdlFiles.iterator()
            else -> wsdlInputDir.asFileTree.matching {
                include("**/*.wsdl")
            }.files.ifEmpty { throw GradleException("Could not find any WSDL files to import") }.iterator()
        }

        var dependentFiles = project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME).get().resolve()
        project.logger.debug("Loading JAR files: $dependentFiles")

        val originalClassLoader = Thread.currentThread().contextClassLoader
        URLClassLoader(dependentFiles.map { it.toURI().toURL() }.toTypedArray()).use { classLoader ->
            val wsdlToJavaClass = classLoader.loadClass("org.apache.cxf.tools.wsdlto.WSDLToJava")
            val toolContextClass = classLoader.loadClass("org.apache.cxf.tools.common.ToolContext")
            Thread.currentThread().contextClassLoader = classLoader
            try {
                computedWsdlFiles.forEach { wsdlFile ->
                    project.logger.info("Importing file ${wsdlFile.absolutePath}")

                    val defaultargs = arrayOf(
                            "-verbose",
                            "-wsdlLocation",
                            wsdlFile.relativeTo(wsdlInputDir.asFile.get()).invariantSeparatorsPath,
                            "-suppress-generated-date",
                            "-autoNameResolution",
                            "-d",
                            sourcesOutputDir.get().toString(),
                            wsdlFile.path
                    )
                    val args = if (bindingFile.get().isBlank()) {
                      defaultargs
                    } else {
                      arrayOf(
                            "-b",
                            bindingFile.get().toString()
                      ) + defaultargs
                    }

                    val toolContext = toolContextClass.getConstructor().newInstance()
                    val wsdlToJava = wsdlToJavaClass.getConstructor(Array<String>::class.java).newInstance(args)
                    wsdlToJavaClass.getMethod("run", toolContextClass).invoke(wsdlToJava, toolContext)
                }
            } finally {
                Thread.currentThread().contextClassLoader = originalClassLoader
            }
        }
    }

    private fun getWsdl2JavaExtension() = project.extensions.getByName(WSDL2JAVA_EXTENSION_NAME) as Wsdl2JavaPluginExtension
}
