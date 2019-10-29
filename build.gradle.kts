plugins {
    kotlin("jvm") version "1.3.50"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "com.github.bjornvester"
version = "0.3"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "5.6.3"
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
            description = "Changes: \n" +
                    "- Fix path separator in the WsdlLocation for the generated client when building on Windows"
            tags = listOf("wsdl2java", "cxf", "wsimport")
        }
    }
}
