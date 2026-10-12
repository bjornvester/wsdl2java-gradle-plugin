# AGENTS.md

## Project

Gradle plugin `com.github.bjornvester.wsdl2java` that runs Apache CXF's `wsdl2java`/`xjc` to generate Java from WSDL/XSD. Written in Kotlin (`kotlin-dsl` + `java-gradle-plugin`). Entry point: `Wsdl2JavaPlugin`.

## Layout

- `src/main/kotlin/com/github/bjornvester/wsdl2java/` – plugin, extension, task, worker.
- `src/test/kotlin/com/github/bjornvester/IntegrationTest.kt` – parameterized test that runs `integration-test/` via `GradleRunner` under Gradle **7.6.1, 8.14.6, 9.8.1** (`provideVersions()`).
- `integration-test/` – standalone project applying the plugin:
  - `cxf3/` – javax + CXF 3.5.11, **Java 8** toolchain
  - `cxf4/` – jakarta + CXF 4.1.8, **Java 17** toolchain
  - `buildSrc/.../java-conventions*.gradle.kts` – shared precompiled convention plugins

## Commands

- `./gradlew test` – full verification (multi-version GradleRunner matrix). Slow (minutes).
- `./gradlew check` from `integration-test/` – fast standalone integration test on the local Gradle wrapper (8.1.1).
- CI (`.github/workflows/main.yml`): Ubuntu, JDK 17, `./gradlew check`.

## Toolchains (important)

- Root build runs on a **Java 17 toolchain** but must emit **Java 8 bytecode** (`options.release=8` on `JavaCompile`, `jvmTarget=1.8` on `KotlinCompile`). Keep it this way – the Gradle 9.x test needs the GradleRunner inner build to run on Java 17, and the plugin must keep supporting Java 8.
- The unit test copies `integration-test/` to a temp dir, strips `includeBuild("..")` from `settings.gradle.kts`, and injects the plugin via `withPluginClasspath()`. The inner build runs on the root test JVM (Java 17).
- GradleRunner uses an **isolated Gradle home**, so it cannot see JDKs registered in IntelliJ, and the foojay resolver (`org.gradle.toolchains.foojay-resolver-convention` 0.5.0 in `integration-test/settings.gradle.kts`) may be unreachable on this network (`error code: 1010`). Toolchain discovery therefore relies on `org.gradle.java.installations.paths` in `integration-test/gradle.properties` – it must list a JDK 17 and a JDK 8 path. Keep it in sync if the JDKs move; it's a no-op on CI.

## Conventions & gotchas

- `integration-test/settings.gradle.kts` `include(...)` lines are modified by the unit test – don't rename/reorder subprojects without updating `IntegrationTest.kt`.
- `java-conventions.gradle.kts` pins some transitive deps for CVEs (neethi, commons-lang3, commons-io) – preserve when bumping CXF.
- `wiremock` is pinned to 2.27.2; comment explains why it must not be upgraded.
- Gradle 9 needs `junit-platform-launcher` declared explicitly on the test runtime classpath (already present).
- Verify Java 8 compatibility after build changes: class file major version must stay 52.
