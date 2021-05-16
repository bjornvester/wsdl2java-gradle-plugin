plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

wsdl2java {
    wsdlDir.set(layout.projectDirectory.dir("src/main/wsdl"))
    includes.set(
        listOf(
            "HelloWorldAbstractService.wsdl",
            "nested/HelloWorldNestedService.wsdl"
        )
    )
}
