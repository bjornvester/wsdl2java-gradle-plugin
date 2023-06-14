plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "com.github.bjornvester"
version = "2.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
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
    compileOnly("org.apache.cxf:cxf-tools-wsdlto-core:4.0.2")
    testImplementation("commons-io:commons-io:2.13.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

gradlePlugin {
    website.set("https://github.com/bjornvester/wsdl2java-gradle-plugin")
    vcsUrl.set("https://github.com/bjornvester/wsdl2java-gradle-plugin")
    plugins {
        create("wsdl2JavaPlugin") {
            id = "com.github.bjornvester.wsdl2java"
            description = """Adds the CXF wsdl2java tool to your project.
        |Please see the Github project page for details.""".trimMargin()
            displayName = "Gradle Wsdl2Java plugin"
            tags.set(listOf("wsdl2java", "cxf", "wsimport"))
            implementationClass = "com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin"
            description = "Changes:\n" +
                    "  - Added support for using the jakarta namespace, which is now the default. The older javax namespace can be enabled with a configuration change.\n" +
                    "  - Added support for the Gradle configuration cache.\n" +
                    "  - The Wsdl2Java task now runs with the configured, or default, Java Toolchain.\n" +
                    "  - Minimum required of version of Gradle is now 6.7 (up from 6.0).\n" +
                    "  - The configurations for marking generated code has changed to support a third variant of the Generated annotation. See the README for details."
        }
    }
}
