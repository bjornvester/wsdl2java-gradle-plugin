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
    @ParameterizedTest(name = "Test plugin with Java version {0} and Gradle version {1}")
    @MethodSource("provideVersions")
    fun thePluginWorks(javaVersion: String, gradleVersion: String, @TempDir tempDir: File) {
        runGenericBuild(javaVersion, gradleVersion, tempDir)
    }

    private fun runGenericBuild(javaVersion: String, gradleVersion: String, tempDir: File) {
        copyIntegrationTestProject(tempDir)

        // Remove the "includedBuild" declaration from the settings file
        tempDir.resolve(SETTINGS_FILE).writeText(tempDir.resolve(SETTINGS_FILE).readText().replace("includeBuild(\"..\")", ""))

        if (GradleVersion.version(gradleVersion) < GradleVersion.version("7.0")) {
            // The grouping functionality is not supported in older versions
            tempDir.resolve(SETTINGS_FILE)
                    .writeText(tempDir.resolve(SETTINGS_FILE).readText().replace("\"grouping-test\",", ""))
        }

        if (javaVersion.toInt() < 17) {
            // CXF 4 projects do not support Java < 17
            val settingsContent = tempDir.resolve(SETTINGS_FILE).readText().replace("""\s*"cxf4:.*""".toRegex(), "")
            tempDir.resolve(SETTINGS_FILE).writeText(settingsContent)
        }

        if (GradleVersion.version(gradleVersion) < GradleVersion.version("7.6")) {
            // The Gradle toolchain provisioning is not supported in older versions
            tempDir.resolve(SETTINGS_FILE)
                    .writeText(tempDir.resolve(SETTINGS_FILE).readText().replace("""id\("org.gradle.toolchains.foojay-resolver-convention"\).*""".toRegex(), ""))
        }

        // Set the Java version
        tempDir.resolve(JAVA_CONVENTIONS_FILE)
                .writeText(
                        tempDir.resolve(JAVA_CONVENTIONS_FILE).readText()
                                .replace("JavaLanguageVersion.of(8)", "JavaLanguageVersion.of($javaVersion)")
                )

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
        const val JAVA_CONVENTIONS_FILE = "buildSrc/src/main/kotlin/com.github.bjornvester.wsdl2java.internal.java-conventions.gradle.kts"

        @JvmStatic
        @Suppress("unused")
        fun provideVersions(): Stream<Arguments?>? {
            return Stream.of(
                    // Test various versions of Gradle, using Java 8
                    // This only tests CXF 3 projects
                    Arguments.of("8", "6.7"), // Minimum required version of Gradle
                    Arguments.of("8", "7.6.1"),
                    Arguments.of("8", "8.1.1"),
                    // Test various versions of Java, other than one used above, and using the newest (at this time) version of Gradle
                    // This tests both CXF 3 and 4 projects
                    Arguments.of("11", "8.1.1"),
                    Arguments.of("17", "8.1.1")
            )
        }
    }
}
