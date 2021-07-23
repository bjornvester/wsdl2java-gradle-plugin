package com.github.bjornvester.wsdl2java

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

interface Wsdl2JavaPluginExtensionGroup {
    val name: String
    val wsdlDir: DirectoryProperty
    val includes: ListProperty<String>
    val includesWithOptions: MapProperty<String, List<*>>
    val bindingFile: RegularFileProperty
    val generatedSourceDir: DirectoryProperty
    val options: ListProperty<String>
    val verbose: Property<Boolean>
    val suppressGeneratedDate: Property<Boolean>
    val markGenerated: Property<String>
    val packageName: Property<String>
}
