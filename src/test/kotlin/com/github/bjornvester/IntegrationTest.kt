package com.github.bjornvester

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.lang.management.ManagementFactory
import java.util.stream.Stream

open class IntegrationTest {
    @ParameterizedTest(name = "Test plugin with Gradle version {0}")
    @MethodSource("provideVersions")
    fun thePluginWorks(gradleVersion: String, @TempDir tempDir: File) {
        runGenericBuild(gradleVersion, tempDir)
    }

    private fun runGenericBuild(gradleVersion: String, tempDir: File) {
        copyIntegrationTestProject(tempDir)

        // Remove the "includedBuild" declaration from the settings file
        tempDir.resolve(SETTINGS_FILE).writeText(tempDir.resolve(SETTINGS_FILE).readText().replace("includeBuild(\"..\")", ""))

        if (GradleVersion.version(gradleVersion) < GradleVersion.version("8.1")) {
            // The Gradle configuration cache was not stable until version 8.1
            tempDir.resolve(PROPERTIES_FILE)
                .writeText(tempDir.resolve(PROPERTIES_FILE).readText().replace("org.gradle.configuration-cache=true", ""))
        }

        GradleRunner
            .create()
            .forwardOutput()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("clean", "check", "-i", "-s", "--no-build-cache")
            .withGradleVersion(gradleVersion)
            .withDebug(isDebuggerAttached())
            .build()
    }

    private fun copyIntegrationTestProject(tempDir: File) {
        val rootFolder = File(System.getProperty("GRADLE_ROOT_FOLDER"))
        val integrationTestDir = rootFolder.resolve("integration-test")
        val ignoredDirNames = arrayOf("out", ".gradle", "build")

        FileUtils.copyDirectory(integrationTestDir, tempDir) { copiedResource ->
            ignoredDirNames.none { ignoredDir ->
                copiedResource.isDirectory && copiedResource.name.toString() == ignoredDir
            }
        }
    }

    private fun isDebuggerAttached(): Boolean {
        return ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
    }

    companion object {
        const val SETTINGS_FILE = "settings.gradle.kts"
        const val PROPERTIES_FILE = "gradle.properties"

        @JvmStatic
        @Suppress("unused")
        fun provideVersions(): Stream<Arguments?>? {
            return Stream.of(
                // Test various versions of Gradle
                Arguments.of("7.6.1"), // Minimum required version of Gradle
                Arguments.of("8.1.1")
            )
        }
    }
}
