[![Gradle Plugin Release](https://img.shields.io/badge/Gradle%20plugin-1.6.0-blue.svg?logo=data:image/svg+xml;base64,PHN2ZyBpZD0iTGF5ZXJfMSIgZGF0YS1uYW1lPSJMYXllciAxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA5MCA2Ni4wNiI+PGRlZnM+PHN0eWxlPi5jbHMtMXtmaWxsOiNmZmY7fTwvc3R5bGU+PC9kZWZzPjx0aXRsZT5ncmFkbGUtZWxlcGhhbnQtaWNvbi13aGl0ZS1wcmltYXJ5PC90aXRsZT48cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik04NS4xMSw0LjE4YTE0LjI3LDE0LjI3LDAsMCwwLTE5LjgzLS4zNCwxLjM4LDEuMzgsMCwwLDAsMCwyTDY3LDcuNmExLjM2LDEuMzYsMCwwLDAsMS43OC4xMkE4LjE4LDguMTgsMCwwLDEsNzkuNSwyMC4wNkM2OC4xNywzMS4zOCw1My4wNS0uMzYsMTguNzMsMTZhNC42NSw0LjY1LDAsMCwwLTIsNi41NGw1Ljg5LDEwLjE3YTQuNjQsNC42NCwwLDAsMCw2LjMsMS43M2wuMTQtLjA4LS4xMS4wOEwzMS41MywzM2E2MC4yOSw2MC4yOSwwLDAsMCw4LjIyLTYuMTMsMS40NCwxLjQ0LDAsMCwxLDEuODctLjA2aDBhMS4zNCwxLjM0LDAsMCwxLC4wNiwyQTYxLjYxLDYxLjYxLDAsMCwxLDMzLDM1LjM0bC0uMDksMC0yLjYxLDEuNDZhNy4zNCw3LjM0LDAsMCwxLTMuNjEuOTQsNy40NSw3LjQ1LDAsMCwxLTYuNDctMy43MWwtNS41Ny05LjYxQzQsMzItMi41NCw0Ni41NiwxLDY1YTEuMzYsMS4zNiwwLDAsMCwxLjMzLDEuMTFIOC42MUExLjM2LDEuMzYsMCwwLDAsMTAsNjQuODdhOS4yOSw5LjI5LDAsMCwxLDE4LjQyLDAsMS4zNSwxLjM1LDAsMCwwLDEuMzQsMS4xOUgzNS45YTEuMzYsMS4zNiwwLDAsMCwxLjM0LTEuMTksOS4yOSw5LjI5LDAsMCwxLDE4LjQyLDBBMS4zNiwxLjM2LDAsMCwwLDU3LDY2LjA2SDYzLjFhMS4zNiwxLjM2LDAsMCwwLDEuMzYtMS4zNGMuMTQtOC42LDIuNDYtMTguNDgsOS4wNy0yMy40M0M5Ni40MywyNC4xNiw5MC40MSw5LjQ4LDg1LjExLDQuMThaTTYxLjc2LDMwLjA1bC00LjM3LTIuMTloMGEyLjc0LDIuNzQsMCwxLDEsNC4zNywyLjJaIi8+PC9zdmc+)](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc)
[![GitHub Actions status](https://github.com/bjornvester/wsdl2java-gradle-plugin/workflows/CI/badge.svg)](https://github.com/bjornvester/wsdl2java-gradle-plugin/actions)

# wsdl2java-gradle-plugin
A Gradle plugin for generating Java classes from WSDL files through CXF.

## Requirements and main features
* The plugin requires Gradle 6.0 or later. (Tested with Gradle 6.0 and 7.0.)
* It has been tested with Java 8, 11 and 16. It does not (yet) support running it with a custom toolchain.
* It supports the Gradle build cache (enabled by setting "org.gradle.caching=true" in your gradle.properties file).
* It supports project relocation for the build cache (e.g. you move your project to a new path, or make a new copy/clone of it).
  This is especially useful in a CI context, where you might clone PRs and/or branches for a repository in their own locations.
* It supports parallel execution (enabled with "org.gradle.parallel=true" in your gradle.properties file), though it does not itself run anything in parallel.

## Configuration
Apply the plugin ID "com.github.bjornvester.wsdl2java" as specific in the [Gradle Plugin portal page](https://plugins.gradle.org/plugin/com.github.bjornvester.wsdl2java), e.g. like this:

```kotlin
plugins {
    id("com.github.bjornvester.wsdl2java") version "1.2"
}
```

Put your WSDL and referenced XSD files somewhere in your src/main/resource directory.
By default, the plugin will create Java classes for all the WSDL files in the resource directory.

The generated code will by default end up in the directory build/generated/sources/wsdl2java folder.

You can configure the plugin using the "wsdl2java" extension like this:

```kotlin
wsdl2java {
    // Set properties here...
}
``` 

Here is a list of all available properties:

| Property                   | Type                  | Default                                          | Description                                                                                                          |
|----------------------------|-----------------------|--------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| wsdlDir                    | DirectoryProperty     | "$projectDir/src<br>/main/resources"             | The directory holding the WSDL and referenced XSD files to compile.                                                  |
| includes                   | ListProperty\<String> | \["**/*.wsdl"]                                   | Inclusion filers (Ant style) for which WSDLs to include.                                                             |
| includesWithOptions        | Map\<String, List>    | \[not set\]                                      | Inclusion filters like above, but with individual options. See below.                                                |
| generatedSourceDir         | DirectoryProperty     | "$buildDir/generated<br>/sources/wsdl2java/java" | The output directory for the generated Java sources.<br>Note that it will be deleted when running XJC.               |
| bindingFile                | RegularFileProperty   | \[not set\]                                      | A binding file to use in the schema compiler.                                                                        |
| cxfVersion                 | Provider\<String>     | "3.4.4"                                          | The version of CXF to use.                                                                                           |
| verbose                    | Provider\<Boolean>    | \[not set\]                                      | Enables verbose output from CXF. If not set, it will be be enabled only on the info logging level.                   |
| suppressGeneratedDate      | Provider\<Boolean>    | true                                             | Suppresses generating dates in CXF. Default is true to support reproducible builds and to work with the build cache. |
| markGenerated              | Provider\<String>     | "no"                                             | Adds the @Generated annotation to the generated sources. See below for details as there are some gotchas with this.  |
| packageName                | Provider\<String>     | \[not set\]                                      | Sets the package name for the generated sources                                                                      |
| options                    | ListProperty\<String> | \[empty\]                                        | Additional options to pass to the tool. See [here](https://cxf.apache.org/docs/wsdl-to-java.html) for details.       |
| addCompilationDependencies | Provider\<Boolean>    | true                                             | Adds dependencies to the `implementation` configuration for compiling the generated sources.                         |


### Configure the CXF version
You can specify the version of CXF used for code generation like this:

```kotlin
wsdl2java {
    cxfVersion.set("3.4.4")
}
```

### Configure included WSDL files
By default, the plugin will find all WSDL files in the `wsdlDir` directory, which defaults to `src/main/resources`.
It is important that if you change this, you change it to a folder that contain all resources (e.g. both WSDL and XSDs).
Otherwise, if you make changes to files outside this folder, Gradle will not see them and thus might consider the task up-to-date.

The plugin will set the `wsdlLocation` property of the `@WebServiceClient` to the path for the WSDL file relative to the `wsdlDir` directory.
If you change `wsdlDir` in a way where this no longer makes sense, you need to set the option yourself.
If you have multiple WSDL files, see the section on additional options.

If you have multiple WSDL files and want to only run the tool on some of them, you can use the `includes` property.
Example:

```kotlin
// Only if different from the default 'src/main/resources'
wsdlDir.set(layout.projectDirectory.dir("src/main/wsdl"))

// For Kotlin DSL
includes.set(listOf(
        "src/main/wsdls/MyFirstService.wsdl",
        "src/main/wsdls/MySecondService.wsdl"
))
```

```groovy
// For Groovy DSL
includes = [
    "src/main/wsdls/MyFirstService.wsdl",
    "src/main/wsdls/MySecondService.wsdl"
]
```

### Configure additional options
Besides the options given in the `wsdl2java` extension, you can provide additional options directly to CXF (and XJC) through the `options` property:

```kotlin
// For Kotlin DSL
includes.set(
    listOf(
        "xjc-no-header",
        "xjc-npa"
    ) 
)

// For Groovy DSL
includes = [
  "xjc-no-header",
  "xjc-npa"
]
```

See [here](https://cxf.apache.org/docs/wsdl-to-java.html) for the available options.

#### Configure additional options for individual WSDL files
It is possible to pass options for individual WSDL files.
This is important especially if you need to explicitly configure the `wsdlLocation` option, as it doesn't make to have the same location in all files.
(But note that if you leave it out, the plugin will guess it based on the file location.)

```kotlin
// For Kotlin DSL
includesWithOptions.set(mapOf(
  "**/ServiceA.wsdl" to listOf("-wsdlLocation", "https://example.com/service-a?wsdl"),
  "**/ServiceB.wsdl" to listOf("-wsdlLocation", "https://example.com/service-b?wsdl")
))
```

```groovy
// For Groovy DSL
includesWithOptions = [
  "**/ServiceA.wsdl" : ["-wsdlLocation", "https://example.com/service-a?wsdl"],
  "**/ServiceB.wsdl" : ["-wsdlLocation", "https://example.com/service-b?wsdl"]
]
```

### Configure the output directory
You can optionally specify the directory for the generated source through the `generatedSourceDir` property, which defaults to `buildDir/generated/sources/wsdl2java/java`.
Example:

```kotlin
wsdl2java {
    generatedSourceDir.set(layout.projectDirectory.dir("src/generated/wsdl2java"))
}
```

Note that the directory will be wiped completely on each run, so don't put other source files in it.

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

Note that at the moment, it is not possible to specify more than one binding file in the extension. If you require this, use the `-b` option.

### Configuring package names
The package name for the generated resources can be configured with the `packageName` field:

```kotlin
wsdl2java {
    packageName.set("com.github.bjornvester.wsdl2java.group1")
}

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

### Activating (third party) XJC plugins
To use third party plugins for the underlying XJC tool, supply the relevant dependencies to the `xjcPlugins` configuration.
Then set the plugin options through the `options` property.

For example, to use the "Equals" and "Hashcode" plugin from the [JAXB2 Basics](https://github.com/highsource/jaxb2-basics) project, configure the following:

```kotlin
dependencies {
    implementation("org.jvnet.jaxb2_commons:jaxb2-basics-runtime:1.11.1")
    xjcPlugins("org.jvnet.jaxb2_commons:jaxb2-basics:1.11.1")
}

wsdl2java {
    options.addAll("-xjc-Xequals", "-xjc-XhashCode")
}
```

## Other
The plugin will add the following two dependencies to your `implementation` configuration:

```
jakarta.xml.ws:jakarta.xml.ws-api:2.3.3
jakarta.jws:jakarta.jws-api:1.1.1
```

These are required to compile the generated code.
However, depending on your runtime platform, you may want to exclude them and instead either put them in the `compileOnly` configuration, or use whatever libraries that are provided by the platform.

There are full examples available in the integration-test directory.

If you need to compile additional XML schemas (xsd files) not directly referenced by the wsdl files, you can use the [com.github.bjornvester.xjc](https://plugins.gradle.org/plugin/com.github.bjornvester.xjc) plugin in addition.

## Limitations
The CXF tool will overwrite generated classes from multiple WSDL files if they have the same qualified name.
Especially the `ObjectFactory` might be overwritten, which is annoying.
There is a similar plugin [here](https://github.com/nilsmagnus/wsdl2java) that can merge them together, but it is deprecated.
I hope to port that functionality into this plugin at some point.
