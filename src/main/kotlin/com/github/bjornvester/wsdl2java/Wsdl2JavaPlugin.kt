package com.github.bjornvester.wsdl2java

import com.github.bjornvester.wsdl2java.Wsdl2JavaPluginExtension.Companion.GENERATED_STYLE_JDK9
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.util.GradleVersion

class Wsdl2JavaPlugin : Plugin<Project> {
    companion object {
        const val MINIMUM_GRADLE_VERSION = "7.6"
        const val PLUGIN_ID = "com.github.bjornvester.wsdl2java"
        const val WSDL2JAVA_TASK_NAME = "wsdl2java"
        const val WSDL2JAVA_EXTENSION_NAME = "wsdl2java"
        const val WSDL2JAVA_CONFIGURATION_NAME = "wsdl2java"
        const val XJC_PLUGINS_CONFIGURATION_NAME = "xjcPlugins"
    }

    override fun apply(project: Project) {
        project.logger.info("Applying $PLUGIN_ID to project ${project.name}")
        verifyGradleVersion()
        project.plugins.apply(JavaPlugin::class.java)
        val extension = project.extensions.create(WSDL2JAVA_EXTENSION_NAME, Wsdl2JavaPluginExtension::class.java)
        val wsdl2JavaConfiguration = createResolvableConfiguration(project, WSDL2JAVA_CONFIGURATION_NAME)
        createResolvableConfiguration(project, XJC_PLUGINS_CONFIGURATION_NAME)

        wsdl2JavaConfiguration.defaultDependencies {
            addLater(extension.cxfVersion.map { project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-frontend-jaxws:$it") })
            addLater(extension.cxfVersion.map { project.dependencies.create("org.apache.cxf:cxf-tools-wsdlto-databinding-jaxb:$it") })
            addLater(extension.useJakarta.map { if (it) "3.0.1" else "2.3.3" }.map { project.dependencies.create("jakarta.xml.ws:jakarta.xml.ws-api:$it") })
            addLater(extension.useJakarta.map { if (it) "2.1.1" else "1.3.5" }.map { project.dependencies.create("jakarta.annotation:jakarta.annotation-api:$it") })
            add(project.dependencies.create("org.slf4j:slf4j-simple:1.7.36"))
        }

        project.configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME) {
            // The listProperty thing is a work-around for https://github.com/gradle/gradle/issues/13255
            dependencies.addAllLater(project.objects.listProperty(Dependency::class.java).convention(extension.addCompilationDependencies.map {
                val deps = listOf<Dependency>().toMutableList()
                if (it) {
                    val wsApiVersion = if (extension.useJakarta.get()) "3.0.1" else "2.3.3"
                    val jwsApiVersion = if (extension.useJakarta.get()) "3.0.0" else "1.1.1"
                    deps.add(project.dependencies.create("jakarta.xml.ws:jakarta.xml.ws-api:$wsApiVersion"))
                    deps.add(project.dependencies.create("jakarta.jws:jakarta.jws-api:$jwsApiVersion"))
                    if (extension.markGenerated.get() && extension.generatedStyle.get() != GENERATED_STYLE_JDK9) {
                        val annotationsApiVersion = if (extension.useJakarta.get()) "2.1.1" else "1.3.5"
                        deps.add(project.dependencies.create("jakarta.annotation:jakarta.annotation-api:$annotationsApiVersion"))
                    }
                }
                deps
            }))
        }

        val defaultTask = addWsdl2JavaTask(WSDL2JAVA_TASK_NAME, project, extension)

        extension.groups.all {
            defaultTask.configure {
                enabled = false
            }

            addWsdl2JavaTask(WSDL2JAVA_TASK_NAME + name.replaceFirstChar(Char::titlecase), project, this)
        }
    }

    private fun addWsdl2JavaTask(name: String, project: Project, group: Wsdl2JavaPluginExtensionGroup): TaskProvider<Wsdl2JavaTask> {
        val wsdl2JavaTask = project.tasks.register(name, Wsdl2JavaTask::class.java) {
            wsdlInputDir.convention(group.wsdlDir)
            includes.convention(group.includes)
            includesWithOptions.convention(group.includesWithOptions)
            bindingFiles.from(group.bindingFiles)
            options.convention(group.options)
            verbose.convention(group.verbose)
            suppressGeneratedDate.convention(group.suppressGeneratedDate)
            markGenerated.convention(group.markGenerated)
            sourcesOutputDir.convention(group.generatedSourceDir)
            packageName.convention(group.packageName)
            wsdl2JavaConfiguration.from(project.configurations.named(WSDL2JAVA_CONFIGURATION_NAME))
            xjcPluginsConfiguration.from(project.configurations.named(XJC_PLUGINS_CONFIGURATION_NAME))

            val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)
            val currentJavaToolchain = project.extensions.getByType(JavaPluginExtension::class.java).toolchain
            val currentJvmLauncherProvider = toolchainService.launcherFor(currentJavaToolchain)
            javaLauncher.convention(currentJvmLauncherProvider)
        }

        val sourceSets = project.properties["sourceSets"] as SourceSetContainer
        sourceSets.named(MAIN_SOURCE_SET_NAME) {
            java.srcDir(wsdl2JavaTask)
        }

        return wsdl2JavaTask
    }

    private fun verifyGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version(MINIMUM_GRADLE_VERSION)) {
            throw UnsupportedOperationException(
                "Plugin $PLUGIN_ID requires at least Gradle $MINIMUM_GRADLE_VERSION, " +
                        "but you are using ${GradleVersion.current().version}"
            )
        }
    }

    private fun createResolvableConfiguration(project: Project, name: String): Configuration {
        return project.configurations.maybeCreate(name).apply {
            isCanBeConsumed = false
            isCanBeResolved = true
            isVisible = false
        }
    }
}
