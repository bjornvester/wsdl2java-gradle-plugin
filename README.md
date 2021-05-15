# wsdl2java-gradle-plugin
A Gradle plugin for generating Java classes from WSDL files through CXF.

## Requirements, features and limitations
* The plugin requires Gradle 6.0 or later. (Tested with Gradle 6.0 and 7.0.)
* It has been tested with Java 8, 11 and 16. It does not (yet) support running it with a custom toolchain.
* It supports the Gradle build cache (enabled by setting "org.gradle.caching=true" in your gradle.properties file).
* It supports project relocation for the build cache (e.g. you move your project to a new path, or make a new copy/clone of it).
  This is especially useful in a CI context, where you might clone PRs and/or branches for a repository in their own locations.
* It supports parallel execution (enabled with "org.gradle.parallel=true", possibly along with "org.gradle.priority=low", in your gradle.properties file).

## Configuration
Apply the plugin ID "com.github.bjornvester.wsdl2java" as specific in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.bjornvester.wsdl2java), e.g. like this:

```kotlin
plugins {
    id("com.github.bjornvester.wsdl2java") version "1.0-snapshot"
}
```

Put your WSDL and referenced XSD files somewhere in your src/main/resource directory.
By default, the plugin will create Java classes for all the WSDL files in the resource directory.

The generated code will by default end up in the directory build/generated/wsdl2java folder.

You can configure the plugin using the "wsdl2java" extension like this:

```kotlin
wsdl2java {
    // Set properties here...
}
``` 

Here is a list of all available properties:

| Property              | Type                  | Default                                                                              | Description                                                                                                         |
|-----------------------|-----------------------|--------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| wsdlDir               | DirectoryProperty     | "$projectDir/src<br>/main/resources"                                                 | The directory holding the xsd files to compile.                                                                     |
| wsdlFiles             | FileCollection        | wsdlDir<br>&nbsp;&nbsp;.asFileTree<br>&nbsp;&nbsp;.matching { include("**/*.wsdl") } | The schemas to compile.<br>If empty, all files in the xsdDir will be compiled.                                      |
| generatedSourceDir    | DirectoryProperty     | "$buildDir/generated<br>/sources/wsdl2java/java"                                     | The output directory for the generated Java sources.<br>Note that it will be deleted when running XJC.              |
| bindingFile           | RegularFileProperty   | \[not set\]                                                                          | A binding file to use in the schema compiler                                                                        |
| cxfVersion            | Provider\<String>     | "3.4.3"                                                                              | The version of CXF to use.                                                                                          |
| verbose               | Provider\<Boolean>    | true                                                                                 | Enables verbose output from CXF.                                                                                    |
| suppressGeneratedDate | Provider\<Boolean>    | true                                                                                 | Suppresses generating dates in CXF. Default is true to enable reproducible builds and to work with the build cache. |
| markGenerated         | Provider\<Boolean>    | "no"                                                                                | Adds the @Generated annotation to the generated sources. See below for details as there are some gotchas with this. |                                                              |
| options               | ListProperty\<String> | \[empty\]                                                                            | Additional options to pass to the tool. See [here](https://cxf.apache.org/docs/wsdl-to-java.html) for details.      |


### Configure the CXF version
You can specify the version of CXF used for code generation like this:

```kotlin
wsdl2java {
    cxfVersion.set("3.4.3")
}
```

### Configure directories
You can optionally specify WSDL and generated source directories like this:

```kotlin
wsdl2java {
    generatedSourceDir.set(layout.projectDirectory.file("src/generated/wsdl2java"))
    wsdlDir.set(layout.projectDirectory.dir("src/main/resources"))
}
```

Note that the `generatedSourceDir` will be wiped completely on each run, so don't put other source files in it.

The `wsdlDir` is only used for up-to-date checking. It must contain all resources used by the WSDLs (e.g. included XSDs as well).

By default, the plugin will find all WSDL files in the `wsdlDir` directory.
To specify other files, you can use the `wsdlFiles` property.
This is a `ConfigurableFileCollection` and can be configured like this:

```kotlin
wsdlFiles.setFrom(
    "src/main/resources/HelloWorldAbstractService.wsdl",
    "src/main/resources/nested/HelloWorldNestedService.wsdl"
)
```

### Configure binding files

A binding file can be added like this:

```kotlin
wsdl2java {
    bindingFile.set(layout.projectDirectory.file("src/main/bindings/binding.xjb"))
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

```kotlin
dependencies {
    implementation("io.github.threeten-jaxb:threeten-jaxb-core:1.2")
}

wsdl2java {
    bindingFile.set(layout.projectDirectory.file("src/main/bindings/bindings.xjb"))
}
```

```xml
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

If you are on a POSIX operating system (e.g. Linux), you may in addition to this need to set your operating system locale to one that supports your encoding.
Otherwise, Java (and therefore also Gradle and CXF) may not be able to create files with names outside of what your default locale supports.
Especially some Docker images, like the Java ECR images from AWS, are by default set to a locale supporting ASCII only.
If this is the case for you, and you want to use UTF-8, you could export an environment variable like this:

```shell script
export LANG=C.UTF-8
```

### Enabling the use of the @Generated annotation
CXF (and the underlying XJC tool) can add a `@Generated` annotation to the generated source code.
This is a source annotation useful for marking classes as generated by a tool.
It can also be used by some static code analytic tools to skip these classes.
Note that it is a source code annotation and thus won't work with tools that work with byte code.

While very useful in theory, there are some gotchas with this annotation.
The main one is that here are actually two @Generated annotations.
The first is 'javax.annotation.Generated' and is available in the JDK up til and including Java 8.
The second is 'javax.annotation.processing.Generated' and is available in the JDK from Java 9.
If your project is using Java 8, you will want to use the former.
However, if you are on Java 9 or later, you may still want to use the former if the tools you use only supports that one.
In that case, you can include the annotation class as a dependency.

To support these different use cases, you can chose which annotation to use with the `markGenerated` property.
Supported values are: `no`, `yes-jdk8` and `yes-jdk9`.
Example:

```kotlin
dependencies {
    // The dependency below is only needed if using the Java 8 version of @Generated (through "yes-jdk8") on Java 9 or later
    // If using "yes-jdk9", it is unnecessary
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

wsdl2java {
    markGenerated.set("yes-jdk8")
}
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
