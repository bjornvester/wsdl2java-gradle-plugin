plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions-cxf4")
}

dependencies {
    testImplementation("commons-io:commons-io:2.8.0")
}

wsdl2java {
    includesWithOptions.set(mapOf(
        "**/HelloWorldAService.wsdl" to listOf("-wsdlLocation", "MyLocationA"),
        "**/HelloWorldBService.wsdl" to listOf("-wsdlLocation", "MyLocationB")
    ))
}
