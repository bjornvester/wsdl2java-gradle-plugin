plugins {
    id("java")
}

repositories {
    mavenCentral()
}

// Default dependencies
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.apache.cxf:cxf-rt-frontend-jaxws")
    testImplementation("com.github.tomakehurst:wiremock:2.27.2") // Note that wiremock can't be upgraded to a higher version, nor use the jdk8 variant, as some transitive libraries will not be compatible with this version of CXF

    testRuntimeOnly("org.apache.cxf:cxf-rt-transports-http")
    testRuntimeOnly("org.apache.cxf:cxf-rt-transports-http-jetty")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.36")
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}
