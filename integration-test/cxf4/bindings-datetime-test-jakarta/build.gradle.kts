plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions-cxf4")
}

dependencies {
    implementation("io.github.threeten-jaxb:threeten-jaxb-core:2.1.0")
}

wsdl2java {
    bindingFile("src/main/bindings/bindings.xml")
}
