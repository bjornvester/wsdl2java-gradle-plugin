plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

wsdl2java {
    wsdlFiles.setFrom(
        "src/main/resources/HelloWorldAbstractService.wsdl",
        "src/main/resources/nested/HelloWorldNestedService.wsdl"
    )
}
