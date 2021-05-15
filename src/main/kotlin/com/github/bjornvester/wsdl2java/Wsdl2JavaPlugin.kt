package com.github.bjornvester.wsdl2java

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GradleVersion


@Suppress("unused")
class Wsdl2JavaPlugin : Plugin<Project> {
    companion object {
        const val MINIMUM_GRADLE_VERSION = "6.0"
        const val PLUGIN_ID = "com.github.bjornvester.wsdl2java"
        const val WSDL2JAVA_TASK_NAME = "wsdl2java"
        const val WSDL2JAVA_EXTENSION_NAME = "wsdl2java"
        const val WSDL2JAVA_CONFIGURATION_NAME = "wsdl2java"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying $PLUGIN_ID to project ${project.name}")
        verifyGradleVersion()
        project.plugins.apply(JavaPlugin::class.java)
        val extension = project.extensions.create(WSDL2JAVA_EXTENSION_NAME, Wsdl2JavaPluginExtension::class.java)
        val wsdl2JavaConfiguration = project.configurations.maybeCreate(WSDL2JAVA_CONFIGURATION_NAME)

        wsdl2JavaConfiguration.defaultDependencies {
            addLater(extension.cxfVersion.map { project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-frontend-jaxws:$it") })
            addLater(extension.cxfVersion.map { project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-databinding-jaxb:$it") })
            add(project.dependencies.create("jakarta.xml.ws:jakarta.xml.ws-api:2.3.3"))
            add(project.dependencies.create("javax.annotation:javax.annotation-api:1.3.2"))
            add(project.dependencies.create("org.slf4j:slf4j-simple:1.7.30"))
        }

        project.configurations.named("implementation") {
            dependencies.add(project.dependencies.create("jakarta.xml.ws:jakarta.xml.ws-api:2.3.3"))
            dependencies.add(project.dependencies.create("jakarta.jws:jakarta.jws-api:1.1.1"))
        }

        project.tasks.register(WSDL2JAVA_TASK_NAME, Wsdl2JavaTask::class.java) {
            val sourceSets = project.properties["sourceSets"] as SourceSetContainer

            sourceSets.named(MAIN_SOURCE_SET_NAME) {
                java.srcDir(sourcesOutputDir)
            }
        }

        project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            dependsOn(WSDL2JAVA_TASK_NAME)
        }
    }

    private fun verifyGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version(MINIMUM_GRADLE_VERSION)) {
            throw UnsupportedOperationException("Plugin $PLUGIN_ID requires at least Gradle $MINIMUM_GRADLE_VERSION, " +
                    "but you are using ${GradleVersion.current().version}")
        }
    }

}