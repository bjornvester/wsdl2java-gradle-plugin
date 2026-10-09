import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "com.github.bjornvester"
version = "2.0.3"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// Compile down to Java 8 bytecode so the plugin keeps supporting Java 8,
// even though Gradle (and thus the test JVM) runs on Java 17.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

// The integration test folder is an input to the unit test in the root project
// Register these files as inputs
tasks.withType<Test>().configureEach {
    inputs
        .files(layout.projectDirectory.dir("integration-test").asFileTree.matching {
            exclude("**/build/**")
            exclude("**/gradle/**")
        })
        .withPathSensitivity(PathSensitivity.RELATIVE)
    useJUnitPlatform()
    systemProperty("GRADLE_ROOT_FOLDER", projectDir.absolutePath)
    systemProperty("GRADLE_PLUGIN_VERSION", version)
}

tasks.withType<Wrapper> {
    gradleVersion = "latest"
}

dependencies {
    compileOnly("org.apache.cxf:cxf-tools-wsdlto-core:4.2.3")
    testImplementation(platform("org.junit:junit-bom:5.14.4")) // Version 6+ requires Java 17 and we still support Java 8
    testImplementation("commons-io:commons-io:2.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    website.set("https://github.com/bjornvester/wsdl2java-gradle-plugin")
    vcsUrl.set("https://github.com/bjornvester/wsdl2java-gradle-plugin")
    plugins {
        create("wsdl2JavaPlugin") {
            id = "com.github.bjornvester.wsdl2java"
            displayName = "Gradle Wsdl2Java plugin"
            tags.set(listOf("wsdl2java", "cxf", "wsimport"))
            implementationClass = "com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin"
            description = "Changes:\n" +
                    "  - Fixed missing task dependency on wsdl2java from sourcesJar"
        }
    }
}
