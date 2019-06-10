plugins {
    id("com.github.bjornvester.wsdl2java")
}

repositories {
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("com.github.tomakehurst:wiremock:2.23.2")
    testImplementation("org.apache.cxf:cxf-rt-frontend-jaxws:3.3.2")

    testRuntimeOnly("org.apache.cxf:cxf-rt-transports-http:3.3.2")
    testRuntimeOnly("org.apache.cxf:cxf-rt-transports-http-jetty:3.3.2")
    testRuntimeOnly("com.sun.activation:javax.activation:1.2.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.26")
}

configurations.all {
    exclude("javax.activation-api")
}

tasks.test {
    useJUnitPlatform()
}

wsdl2java {
    cxfVersion.set("3.3.2")
}
