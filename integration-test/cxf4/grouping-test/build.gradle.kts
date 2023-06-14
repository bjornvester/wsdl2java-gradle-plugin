plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions-cxf4")
}

dependencies {
    testImplementation("commons-io:commons-io:2.8.0")
}

wsdl2java {
    groups {
        register("group1") {
            includes.set(listOf("**/HelloWorldAService.wsdl"))
            options.set(listOf("-wsdlLocation", "MyLocationA"))
            packageName.set("com.github.bjornvester.wsdl2java.group1")
        }
        register("group2") {
            includes.set(listOf("**/HelloWorldBService.wsdl"))
            options.set(listOf("-wsdlLocation", "MyLocationB"))
            packageName.set("com.github.bjornvester.wsdl2java.group2")
        }
    }
}
