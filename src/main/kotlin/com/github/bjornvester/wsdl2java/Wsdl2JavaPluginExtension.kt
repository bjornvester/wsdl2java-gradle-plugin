package com.github.bjornvester.wsdl2java

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class Wsdl2JavaPluginExtension @Inject constructor(project: Project) {
    val wsdlDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("src/main/resources"))
    val wsdlFiles: ConfigurableFileCollection = project.objects.fileCollection()
    val bindingFile: Property<String> = project.objects.property(String::class.java).convention("")
    val generatedSourceDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("generated/wsdl2java"))
    val cxfVersion: Property<String> = project.objects.property(String::class.java).convention("3.3.2")
}
