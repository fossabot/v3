@file:Suppress(
  "UnstableApiUsage",
  "unused",
  "UNUSED_VARIABLE",
  "DSL_SCOPE_VIOLATION",
)

plugins {
  java
  jacoco
  idea
  kotlin("jvm")
  kotlin("kapt")
  kotlin("plugin.serialization")
  alias(libs.plugins.micronautApplication)
  alias(libs.plugins.micronautAot)
  alias(libs.plugins.sonar)
}

group = "dev.elide.samples"
version = rootProject.version as String

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
  }
}

tasks.withType<Tar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip>{
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }
  }
}

application {
  mainClass.set("fullstack.ssr.App")
}

micronaut {
  version.set(libs.versions.micronaut.lib.get())
  runtime.set(io.micronaut.gradle.MicronautRuntime.NETTY)
  processing {
    incremental.set(true)
    annotations.add("fullstack.ssr.*")
  }
  aot {
    optimizeServiceLoading.set(true)
    convertYamlToJava.set(true)
    precomputeOperations.set(true)
    cacheEnvironment.set(true)
    netty {
      enabled.set(true)
    }
  }
}

val nodeDist: Configuration by configurations.creating {
  isCanBeConsumed = false
  isCanBeResolved = true
}

dependencies {
  implementation(project(":packages:base"))
  implementation(project(":packages:server"))
  implementation(project(":packages:graalvm"))
  implementation(libs.micronaut.context)
  implementation(libs.micronaut.runtime)
  implementation(libs.kotlinx.html.jvm)
  implementation(libs.kotlinx.wrappers.css)
  runtimeOnly(libs.logback)

  nodeDist(
    project(
      mapOf(
        "path" to ":samples:fullstack:ssr:node",
        "configuration" to "nodeDist",
      )
    )
  )
}

graalvmNative {
  binaries {
    named("main") {
      fallback.set(false)
      buildArgs.addAll(listOf(
        "--language:js",
        "--language:java",
        "--language:regex",
        "--enable-all-security-services",
        "-Dpolyglot.image-build-time.PreinitializeContexts=js",
      ))

      javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
        if (project.hasProperty("elide.graalvm.variant")) {
          val variant = project.property("elide.graalvm.variant") as String
          if (variant != "COMMUNITY") {
            vendor.set(
              JvmVendorSpec.matching(when (variant.trim()) {
                "ENTERPRISE" -> "GraalVM Enterprise"
                else -> "GraalVM Community"
              }))
          }
        }
      })
    }
  }
}

tasks.withType<Copy>().named("processResources") {
  dependsOn("copyStatic")
  dependsOn("copyEmbedded")
}

tasks.register<Copy>("copyStatic") {
  from("src/main/resources/static/**/*.*")
  into("$buildDir/resources/main/static")
}

tasks.register<Copy>("copyEmbedded") {
  from(nodeDist)
  into("$buildDir/resources/main/embedded")
}

tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {
  baseImage("${project.properties["elide.publish.repo.docker.tools"]}/base:latest")
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuild") {
  images.set(listOf(
    "${project.properties["elide.publish.repo.docker.samples"]}/fullstack/ssr/jvm:latest"
  ))
  this.target
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuildNative") {
  images.set(listOf(
    "${project.properties["elide.publish.repo.docker.samples"]}/fullstack/ssr/native:latest"
  ))
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
  graalImage.set("${project.properties["elide.publish.repo.docker.tools"]}/builder:latest")
  baseImage(project.properties["elide.samples.docker.base.native"] as String)
  args("-H:+StaticExecutableWithDynamicLibC")
}
