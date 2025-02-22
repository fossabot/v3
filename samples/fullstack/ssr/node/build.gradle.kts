@file:Suppress(
  "UnstableApiUsage",
  "unused",
  "UNUSED_VARIABLE",
  "DSL_SCOPE_VIOLATION",
)

import com.github.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import elide.dev.buildtools.gradle.tasks.GenerateEsBuildConfig
import elide.dev.buildtools.gradle.tasks.GenerateEsBuildConfig.Mode
import elide.dev.buildtools.gradle.tasks.GenerateEsBuildConfig.Mode.*
import elide.dev.buildtools.gradle.tasks.outputBundleFile
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.RootPackageJsonTask
import java.util.*

plugins {
  idea
  distribution
  kotlin("js")
  kotlin("kapt")
  kotlin("plugin.serialization")
  alias(libs.plugins.node)
  alias(libs.plugins.sonar)
}

group = "dev.elide.samples"
version = rootProject.version as String

val devMode = (project.property("elide.buildMode") ?: "dev") == "dev"

kotlin {
  js(IR) {
    nodejs {
      binaries.executable()
    }
  }
}

dependencies {
  implementation(project(":packages:base"))
  implementation(project(":packages:graalvm-js"))
  implementation(npm("esbuild", libs.versions.npm.esbuild.get()))
}

tasks.withType<Tar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip>{
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val rootPackageJson by rootProject.tasks.getting(RootPackageJsonTask::class)

node {
  download.set(false)
  nodeProjectDir.set(rootPackageJson.rootPackageJson.parentFile.normalize())
}

tasks {
  val compileProductionExecutableKotlinJs by getting(KotlinJsIrLink::class)

  val fixNodeFetch by creating(Copy::class) {
    dependsOn(compileProductionExecutableKotlinJs)
    from(compileProductionExecutableKotlinJs.outputs.files.files) {
      duplicatesStrategy = DuplicatesStrategy.EXCLUDE
      filter { line ->
        line.replace(
          "tmp = jsRequireNodeFetch()(input, init);",
          "tmp = jsRequireNodeFetch().default(input, init);"
        )
      }
    }
    from(compileProductionExecutableKotlinJs.outputFile) {
      filter { line ->
        line.replace(
          "tmp = jsRequireNodeFetch()(input, init);",
          "tmp = jsRequireNodeFetch().default(input, init);"
        )
      }
      rename { "ssr.js" }
    }
    into("$buildDir/fix")
  }

  Mode.values()
    .map { it to it.name.toLowerCase().capitalize() }
    .forEach { (mode, modeName) ->
      val generateEsBuildConfig =
        create<GenerateEsBuildConfig>("generate${modeName}EsBuildConfig") {
          dependsOn(fixNodeFetch)
          group = "other"
          this.mode = mode // PRODUCTION will fail
          entryFile = fixNodeFetch.destinationDir / "ssr.js"
          libraryName = "embedded"
          outputBundleName = buildString {
            append(project.name)
            when (mode) {
              PRODUCTION -> append("-prod")
              DEVELOPMENT -> append("-dev")
            }
            append(".js")
          }
          outputPrepackedName = buildString {
            append(project.name)
            when (mode) {
              PRODUCTION -> append("-prod")
              DEVELOPMENT -> append("-dev")
            }
            append(".pack.js")
          }
          outputBundleFolder = file("$buildDir/distributions").absolutePath
          processShim = file("$buildDir/esbuild/process-shim.js")
          outputConfig = file("$buildDir/esbuild/esbuild.${modeName.toLowerCase()}.js")
          if (mode == PRODUCTION) {
            minify = true
          }
        }

      val embeddedExecutable = create<NodeTask>("${modeName.toLowerCase()}EmbeddedExecutable") {
        group = "distribution"
        dependsOn(generateEsBuildConfig)
        script.set(generateEsBuildConfig.outputConfig)

        setNodeModulesPath(rootPackageJson.rootPackageJson.parentFile / "node_modules")

        inputs.file(generateEsBuildConfig.processShim)
        inputs.file(generateEsBuildConfig.outputConfig)
        inputs.file(fixNodeFetch.destinationDir / "ssr.js")
        outputs.file(generateEsBuildConfig.outputBundleFile)
      }
    }
}

val nodeDist by configurations.creating {
  isCanBeConsumed = true
  isCanBeResolved = false
}

artifacts {
  add(nodeDist.name, tasks.named("developmentEmbeddedExecutable").map {
    project.logger.lifecycle("Assembling embedded SSR bundle (${it.outputs.files.joinToString(", ")})...")
    it.outputs.files.files.single()
  })
}

@Suppress("UnstableApiUsage")
fun NodeTask.setNodeModulesPath(path: String) =
  environment.put("NODE_PATH", path)

@Suppress("UnstableApiUsage")
fun NodeTask.setNodeModulesPath(folder: File) =
  environment.put("NODE_PATH", folder.normalize().absolutePath)

fun File.child(name: String) =
  File(this, name)

operator fun File.div(name: String) =
  child(name)
