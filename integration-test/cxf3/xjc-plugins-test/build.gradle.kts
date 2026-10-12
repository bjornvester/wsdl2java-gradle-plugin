plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions-cxf3")
}

dependencies {
    implementation("org.jvnet.jaxb2_commons:jaxb2-basics-runtime:2.0.12")
    xjcPlugins("org.jvnet.jaxb2_commons:jaxb2-basics:2.0.12")
}

wsdl2java {
    options.addAll("-xjc-Xequals", "-xjc-XhashCode")
    useJakarta.set(false)
}
