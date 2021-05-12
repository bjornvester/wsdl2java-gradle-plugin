plugins {
    id("com.github.bjornvester.wsdl2java")
    id("com.github.bjornvester.wsdl2java.internal.java-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.apache.cxf:cxf-bom:3.4.3"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("com.github.tomakehurst:wiremock:2.27.2")
    testImplementation("org.apache.cxf:cxf-rt-frontend-jaxws")

    testRuntimeOnly("javax.annotation:javax.annotation-api:1.3.2")
    testRuntimeOnly("org.apache.cxf:cxf-rt-transports-http")
    testRuntimeOnly("org.apache.cxf:cxf-rt-transports-http-jetty")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.30")
}

tasks.test {
    useJUnitPlatform()
}

wsdl2java {
    cxfVersion.set("3.4.2")
}
