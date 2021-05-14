package com.github.bjornvester.wsdl2java

import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_CONFIGURATION_NAME
import com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin.Companion.WSDL2JAVA_EXTENSION_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
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
    val wsdlInputDir: DirectoryProperty = objects.directoryProperty().convention(getWsdl2JavaExtension().wsdlDir)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    val wsdlFiles: ConfigurableFileCollection = objects.fileCollection().from(getWsdl2JavaExtension().wsdlFiles)

    @get:Input
    @Optional
    val bindingFile: Property<String> = objects.property(String::class.java).convention(getWsdl2JavaExtension().bindingFile)

    @get:OutputDirectory
    val sourcesOutputDir: DirectoryProperty = objects.directoryProperty().convention(getWsdl2JavaExtension().generatedSourceDir)

    init {
        group = BasePlugin.BUILD_GROUP
        description = "Generates Java classes from WSDL files."
        dependsOn(project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME)) // TODO: Can't remember why this was added. Figure out why, and if so, add a comment about it
    }

    @TaskAction
    fun doCodeGeneration() {
        fileOperations.delete(sourcesOutputDir)
        fileOperations.mkdir(sourcesOutputDir)

        val computedWsdlFiles = when {
            !wsdlFiles.isEmpty -> wsdlFiles.iterator()
            else -> wsdlInputDir.asFileTree.matching {
                include("**/*.wsdl")
            }.files.ifEmpty { throw GradleException("Could not find any WSDL files to import") }.iterator()
        }

        val workerExecutor = workerExecutor.classLoaderIsolation {
            classpath.from(project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME).get().resolve())
        }

        computedWsdlFiles.forEach { wsdlFile ->
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
            val computedArgs = if (bindingFile.get().isBlank()) {
                defaultargs
            } else {
                arrayOf(
                    "-b",
                    bindingFile.get()
                ) + defaultargs
            }
            workerExecutor.submit(Wsdl2JavaWorker::class.java) {
                args = computedArgs
            }
        }

    }

    private fun getWsdl2JavaExtension() = project.extensions.getByName(WSDL2JAVA_EXTENSION_NAME) as Wsdl2JavaPluginExtension
}
