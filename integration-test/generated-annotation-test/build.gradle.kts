plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

dependencies {
    implementation("javax.annotation:javax.annotation-api:1.3.2") // For the @Generated annotation from JDK8 but running in JDK9+
    testImplementation("commons-io:commons-io:2.8.0")
}

wsdl2java {
    markGenerated.set("yes-jdk8")
}
