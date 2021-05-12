package com.github.bjornvester.wsdl2java

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class Wsdl2JavaPluginExtension @Inject constructor(objects: ObjectFactory, layout: ProjectLayout) {
    val wsdlDir: DirectoryProperty = objects.directoryProperty().convention(layout.projectDirectory.dir("src/main/resources"))
    val wsdlFiles: ConfigurableFileCollection = objects.fileCollection()
    val bindingFile: Property<String> = objects.property(String::class.java).convention("")
    val generatedSourceDir: DirectoryProperty = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/wsdl2java"))
    val cxfVersion: Property<String> = objects.property(String::class.java).convention("3.4.3")
}
