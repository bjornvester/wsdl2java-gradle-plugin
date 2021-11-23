package com.github.bjornvester.wsdl2java

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class Wsdl2JavaPluginExtension @Inject constructor(objects: ObjectFactory, layout: ProjectLayout) : Wsdl2JavaPluginExtensionGroup {
    val cxfVersion = objects.property(String::class.java).convention("3.4.4")
    val addCompilationDependencies = objects.property(Boolean::class.java).convention(true)

    override val name = "Defaults"
    override val wsdlDir = objects.directoryProperty().convention(layout.projectDirectory.dir("src/main/resources"))
    override val includes = objects.listProperty(String::class.java).convention(listOf("**/*.wsdl"))
    override val includesWithOptions = objects.mapProperty(String::class.java, List::class.java)
    override val bindingFile = objects.fileProperty()
    override val bindingFiles = objects.fileCollection()
    override val generatedSourceDir = objects.directoryProperty().convention(layout.buildDirectory.dir("generated/sources/wsdl2java/java"))
    override val options = objects.listProperty(String::class.java)
    override val verbose = objects.property(Boolean::class.java)
    override val suppressGeneratedDate = objects.property(Boolean::class.java).convention(true)
    override val markGenerated = objects.property(String::class.java).convention(MARK_GENERATED_NO)
    override val packageName = objects.property(String::class.java)

    val groups: NamedDomainObjectContainer<Wsdl2JavaPluginExtensionGroup> = objects.domainObjectContainer(Wsdl2JavaPluginExtensionGroup::class.java)

    init {
        groups.configureEach {
            wsdlDir.convention(this@Wsdl2JavaPluginExtension.wsdlDir)
            includes.convention(this@Wsdl2JavaPluginExtension.includes)
            includesWithOptions.convention(this@Wsdl2JavaPluginExtension.includesWithOptions)
            bindingFile.convention(this@Wsdl2JavaPluginExtension.bindingFile)
            bindingFiles.from(this@Wsdl2JavaPluginExtension.bindingFiles)
            generatedSourceDir.convention(layout.buildDirectory.dir("generated/sources/wsdl2java-$name/java"))
            options.convention(this@Wsdl2JavaPluginExtension.options)
            verbose.convention(this@Wsdl2JavaPluginExtension.verbose)
            suppressGeneratedDate.convention(this@Wsdl2JavaPluginExtension.suppressGeneratedDate)
            markGenerated.convention(this@Wsdl2JavaPluginExtension.markGenerated)
            packageName.convention(this@Wsdl2JavaPluginExtension.packageName)
        }
    }

    companion object {
        @JvmStatic
        val MARK_GENERATED_NO = "no"

        @JvmStatic
        val MARK_GENERATED_YES_JDK8 = "yes-jdk8"

        @JvmStatic
        val MARK_GENERATED_YES_JDK9 = "yes-jdk9"
    }
}
