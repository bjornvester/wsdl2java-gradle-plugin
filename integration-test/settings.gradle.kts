plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

includeBuild("..")

include(
    "bindings-datetime-test",
    "filter-test",
    "generated-annotation-test",
    "grouping-test",
    "includes-options-test",
    "utf8-test",
    "xjc-plugins-test"
)
