package com.github.bjornvester.wsdl2java

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class Wsdl2JavaPluginExtension @Inject constructor(project: Project) {
    var wsdlDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.projectDirectory.dir("src/main/resources"))
    var wsdlFiles: ConfigurableFileCollection = project.objects.fileCollection()
    var generatedSourceDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("generated/wsdl2java"))
    var cxfVersion: Property<String> = project.objects.property(String::class.java).convention("3.3.2")
}
