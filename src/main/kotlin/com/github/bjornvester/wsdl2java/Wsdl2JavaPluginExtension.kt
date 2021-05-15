package com.github.bjornvester.wsdl2java

import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class Wsdl2JavaPluginExtension @Inject constructor(objects: ObjectFactory, layout: ProjectLayout) {
    val wsdlDir = objects.directoryProperty().convention(layout.projectDirectory.dir("src/main/resources"))
    val wsdlFiles = objects.fileCollection().from(wsdlDir.asFileTree.matching { include("**/*.wsdl") })
    val bindingFile = objects.property(String::class.java).convention("")
    val generatedSourceDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/wsdl2java"))
    val cxfVersion = objects.property(String::class.java).convention("3.4.3")
}
