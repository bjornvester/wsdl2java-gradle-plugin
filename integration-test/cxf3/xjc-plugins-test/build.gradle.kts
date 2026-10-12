plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions-cxf3")
}

dependencies {
    implementation("org.jvnet.jaxb:jaxb2-basics-runtime:2.0.15")
    xjcPlugins("org.jvnet.jaxb:jaxb2-basics:2.0.15")
}

wsdl2java {
    options.addAll("-xjc-Xequals", "-xjc-XhashCode")
    useJakarta.set(false)
}
