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

    // Raise vulnerable transitive versions pulled in by CXF / wiremock to patched releases.
    // These are same-major, minimal bumps that fix published CVEs without changing behaviour.
    constraints {
        // CVE-2026-42402 / CVE-2026-42403 / CVE-2026-42404 (fixed in 3.2.2)
        testImplementation("org.apache.neethi:neethi:3.2.2")
        // CVE-2025-48924 (fixed in 3.18.0)
        testImplementation("org.apache.commons:commons-lang3:3.18.0")
        // CVE-2024-47554 / CVE-2021-29425 (fixed in 2.14.0)
        testImplementation("commons-io:commons-io:2.14.0")
    }
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}
