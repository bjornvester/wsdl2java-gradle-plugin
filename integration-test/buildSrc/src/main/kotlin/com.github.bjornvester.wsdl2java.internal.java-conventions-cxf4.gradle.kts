plugins {
    id("java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.apache.cxf:cxf-bom:4.0.2"))
    testRuntimeOnly("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
}

java {
    toolchain {
        // Note that the unit test in the root project modifies the line below
        // Be careful making changes
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}