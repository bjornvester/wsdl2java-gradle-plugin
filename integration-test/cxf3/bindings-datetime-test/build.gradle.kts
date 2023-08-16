plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions-cxf3")
}

dependencies {
    implementation("io.github.threeten-jaxb:threeten-jaxb-core:1.2")
}

wsdl2java {
    useJakarta.set(false)
    bindingFile("src/main/bindings/bindings.xml")
}
