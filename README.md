# wsdl2java-gradle-plugin
A Gradle plugin for generating Java classes from WSDL files through CXF.

## Requirements and limitations
The plugin currently requires Gradle 5.4 or later.
I hope to make it work with earlier versions as well at some point.

It has been tested with Java 8 and Java 11.

It is currently not possible to customize the CXF code generation tool.
This will be implemented later.

## Configuration
Apply the plugin ID "com.github.bjornvester.wsdl2java" as specific in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.bjornvester.wsdl2java), e.g. like this:

```kotlin
plugins {
    id("com.github.bjornvester.wsdl2java") version "0.4"
}
```

Put your WSDL and referenced XSD files somewhere in your src/main/resource directory.
By default, the plugin will create Java classes for all the WSDL files in the resource directory.

The generated code will by default end up in the directory build/generated/wsdl2java folder.

You can specify the version of CXF used for code generation like this:

```kotlin
wsdl2java {
    cxfVersion.set("3.3.2")
}
```

You can optionally specify WSDL and generated source directories like this:

```groovy
wsdl2java {
    generatedSourceDir = file("${projectDir}/src/main/service")
    wsdlDir = file("${projectDir}/src/main/resources")
}
```

If your WSDL files breach the `theEnumMemberSizeCap` limit, you can specify a
JAXB binding file to raise the `theEnumMemberSizeCap` limit like this:

```groovy
wsdl2java {
    bindingFile = "${projectDir}/src/main/resources/binding.xjb"
}
```

And add a `src/main/resources/binding.xjb` file, e.g.

```xml
<jxb:bindings xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:jxb="http://java.sun.com/xml/ns/jaxb" version="2.1">

    <!-- Raise theEnumMemberSizeCap limit -->
    <jxb:bindings>
        <jxb:globalBindings typesafeEnumMaxMembers="2000" />
    </jxb:bindings>

</jxb:bindings>
```

If your WSDL files include non-ANSI characters, you should set the corresponding file encoding in your gradle.properties file. E.g.:

```properties
org.gradle.jvmargs=-Dfile.encoding=UTF-8
```

Note that the plugin will add the following two dependencies to your "implementation" configuration:

```
jakarta.xml.ws:jakarta.xml.ws-api:2.3.2
jakarta.jws:jakarta.jws-api:1.1.1
```

These are required to compile the generated code.
However, depending on your runtime platform, you may want to exclude them and instead either put them in the "compileOnly" configuration, or use whatever libraries that are provided by the platform.

There is a full example available in the integration-test directory.

If you need to compile additional XMl schemas (xsd files) not directly referenced by the wsdl files, you can use the [com.github.bjornvester.xjc](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc) plugin in addition.
