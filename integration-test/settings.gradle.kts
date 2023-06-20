plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.5.0")
}

includeBuild("..")

include(
    // Note that the lines below are modified by the unit test in the root project.
    // Be careful making changes.
    "cxf4:bindings-datetime-test-jakarta",
    "cxf4:filter-test",
    "cxf4:grouping-test",
    "cxf4:includes-options-test",
    "cxf4:utf8-test",

    "cxf3:bindings-datetime-test",
    "cxf3:generated-annotation-test",
    "cxf3:xjc-plugins-test"
)
