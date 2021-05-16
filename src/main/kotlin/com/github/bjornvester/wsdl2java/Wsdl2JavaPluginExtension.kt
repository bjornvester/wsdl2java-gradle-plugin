package com.github.bjornvester.wsdl2java

import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class Wsdl2JavaPluginExtension @Inject constructor(objects: ObjectFactory, layout: ProjectLayout) {
    val wsdlDir = objects.directoryProperty().convention(layout.projectDirectory.dir("src/main/resources"))
    val includes = objects.listProperty(String::class.java).convention(listOf("**/*.wsdl"))
    val bindingFile = objects.fileProperty()
    val generatedSourceDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/sources/wsdl2java"))
    val cxfVersion = objects.property(String::class.java).convention("3.4.3")
    val options = objects.listProperty(String::class.java)
    val verbose = objects.property(Boolean::class.java).convention(true)
    val suppressGeneratedDate = objects.property(Boolean::class.java).convention(true)
    val markGenerated = objects.property(String::class.java).convention(MARK_GENERATED_NO)

    companion object {
        @JvmStatic
        val MARK_GENERATED_NO = "no"

        @JvmStatic
        val MARK_GENERATED_YES_JDK8 = "yes-jdk8"

        @JvmStatic
        val MARK_GENERATED_YES_JDK9 = "yes-jdk9"
    }
}
