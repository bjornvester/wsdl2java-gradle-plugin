plugins {
    id("java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.apache.cxf:cxf-bom:3.5.6"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
