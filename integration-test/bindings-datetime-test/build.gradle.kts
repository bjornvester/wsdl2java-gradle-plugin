plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

dependencies {
    implementation("io.github.threeten-jaxb:threeten-jaxb-core:1.2")
}

wsdl2java {
    bindingFile.set("$projectDir/src/main/bindings/bindings.xml")
}
