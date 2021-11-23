plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.18.0"
}

group = "com.github.bjornvester"
version = "1.3"

repositories {
    mavenCentral()
}

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
    gradleVersion = "7.3"
}

dependencies {
    compileOnly("org.apache.cxf:cxf-tools-wsdlto-core:3.4.5")
    testImplementation("commons-io:commons-io:2.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

gradlePlugin {
    plugins {
        create("wsdl2JavaPlugin") {
            id = "com.github.bjornvester.wsdl2java"
            implementationClass = "com.github.bjornvester.wsdl2java.Wsdl2JavaPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/bjornvester/wsdl2java-gradle-plugin"
    vcsUrl = "https://github.com/bjornvester/wsdl2java-gradle-plugin"
    description = """Adds the CXF wsdl2java tool to your project. Works with Java 8 and 11, and supports the Gradle build cache.
        |Please see the Github project page for details.""".trimMargin()
    (plugins) {
        "wsdl2JavaPlugin" {
            displayName = "Gradle Wsdl2Java plugin"
            tags = listOf("wsdl2java", "cxf", "wsimport")
            description = "Changes:\n"
                    "  - Add support for multiple binding files."
        }
    }
}
