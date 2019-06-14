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
        const val MINIMUM_GRADLE_VERSION = "5.4"
        const val PLUGIN_ID = "com.github.bjornvester.wsdl2java"
        const val WSDL2JAVA_TASK_NAME = "wsdl2java"
        const val WSDL2JAVA_EXTENSION_NAME = "wsdl2java"
        const val WSDL2JAVA_CONFIGURATION_NAME = "wsdl2java"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying $PLUGIN_ID to project ${project.name}")
        verifyGradleVersion()
        project.plugins.apply(JavaPlugin::class.java)
        val extension = project.extensions.create(WSDL2JAVA_EXTENSION_NAME, Wsdl2JavaPluginExtension::class.java, project)
        val wsdl2JavaConfiguration = project.configurations.maybeCreate(WSDL2JAVA_CONFIGURATION_NAME)

        wsdl2JavaConfiguration.defaultDependencies {
            it.add(project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-frontend-jaxws:${extension.cxfVersion.get()}"))
            it.add(project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-databinding-jaxb:${extension.cxfVersion.get()}"))
            it.add(project.dependencies.create("jakarta.xml.ws:jakarta.xml.ws-api:2.3.2"))
            it.add(project.dependencies.create("org.slf4j:slf4j-simple:1.7.26"))
        }

        project.configurations.named("implementation") {
            it.dependencies.add(project.dependencies.create("jakarta.xml.ws:jakarta.xml.ws-api:2.3.2"))
            it.dependencies.add(project.dependencies.create("jakarta.jws:jakarta.jws-api:1.1.1"))
        }

        project.tasks.register(WSDL2JAVA_TASK_NAME, Wsdl2JavaTask::class.java) { wsdl2JavaTask ->
            val sourceSets = project.properties["sourceSets"] as SourceSetContainer

            sourceSets.named(MAIN_SOURCE_SET_NAME) {
                it.java.srcDir(wsdl2JavaTask.sourcesOutputDir)
            }
        }

        project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            it.dependsOn(WSDL2JAVA_TASK_NAME)
        }
    }

    private fun verifyGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version(MINIMUM_GRADLE_VERSION)) {
            throw UnsupportedOperationException("Plugin $PLUGIN_ID requires at least Gradle $MINIMUM_GRADLE_VERSION, " +
                    "but you are using ${GradleVersion.current().version}")
        }
    }

}