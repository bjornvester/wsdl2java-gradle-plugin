plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

dependencies {
    implementation("io.github.threeten-jaxb:threeten-jaxb-core:1.2")
    implementation("javax.annotation:javax.annotation-api:1.3.2") // For the @Generated annotation from JDK8 but running in JDK9+
}

wsdl2java {
    bindingFiles.from("src/main/bindings/bindings.xml")
    markGenerated.set("yes-jdk8")
}
