# wsdl2java-gradle-plugin
A Gradle plugin for generating Java classes from WSDL files through CXF.

## Requirements and limitations
The plugin currently requires Gradle 5.4 or later. (Tested with Gradle 5.4 and 7.0.)

It has been tested with Java 8, 11 and 16.

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

### Configure the CXF version
You can specify the version of CXF used for code generation like this:

```kotlin
wsdl2java {
    cxfVersion.set("3.4.3")
}
```

### Configure directories
You can optionally specify WSDL and generated source directories like this:

```groovy
wsdl2java {
    generatedSourceDir = file("${projectDir}/src/main/service")
    wsdlDir = file("${projectDir}/src/main/resources")
}
```

Note that the `generatedSourceDir` will be wiped completely on each run, so don't put other source files in it.

The `wsdlDir` is only used for up-to-date checking. It must contain all resources used by the WSDLs (e.g. included XSDs as well).

### Configure binding files

A binding file can be added like this:

```groovy
wsdl2java {
    bindingFile = "${projectDir}/src/main/bindings/binding.xjb"
}
```

If you get a warning on maximum enum member size, you can raise the maximum like this:

```xml
<jxb:bindings xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:jxb="http://java.sun.com/xml/ns/jaxb" version="2.1">

    <!-- Raise theEnumMemberSizeCap limit -->
    <jxb:bindings>
        <jxb:globalBindings typesafeEnumMaxMembers="2000" />
    </jxb:bindings>

</jxb:bindings>
```

If you like to use the Java Date/Time API instead of the more clunky GregorianCalendar class, you can use `threeten-jaxb` library with a binding file like this:

```groovy
// build.gradle
dependencies {
    implementation("io.github.threeten-jaxb:threeten-jaxb-core:1.2")
}

wsdl2java {
    bindingFile.set("$projectDir/src/main/bindings/bindings.xml")
}
```

```xml
<!-- bindings.xml -->
<bindings xmlns="http://java.sun.com/xml/ns/jaxb" version="2.1"
          xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc">
    <globalBindings>
        <xjc:javaType name="java.time.OffsetDate" xmlType="xs:date"
                      adapter="io.github.threetenjaxb.core.OffsetTimeXmlAdapter"/>
        <xjc:javaType name="java.time.OffsetDateTime" xmlType="xs:dateTime"
                      adapter="io.github.threetenjaxb.core.OffsetDateTimeXmlAdapter"/>
    </globalBindings>
</bindings>
```

### Configure encoding
If your WSDL files include non-ANSI characters, you should set the corresponding file encoding in your gradle.properties file. E.g.:

```properties
org.gradle.jvmargs=-Dfile.encoding=UTF-8
```

## Other
Note that the plugin will add the following two dependencies to your `implementation` configuration:

```
jakarta.xml.ws:jakarta.xml.ws-api:2.3.3
jakarta.jws:jakarta.jws-api:1.1.1
```

These are required to compile the generated code.
However, depending on your runtime platform, you may want to exclude them and instead either put them in the "compileOnly" configuration, or use whatever libraries that are provided by the platform.

There are full examples available in the integration-test directory.

If you need to compile additional XMl schemas (xsd files) not directly referenced by the wsdl files, you can use the [com.github.bjornvester.xjc](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc) plugin in addition.
