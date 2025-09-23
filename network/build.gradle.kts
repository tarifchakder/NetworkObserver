@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Network"
            isStatic = true
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "network.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.coroutinesCore)
        }
        androidMain.dependencies {
            implementation(libs.startupRuntime)
        }
        jvmMain.dependencies {
            implementation(libs.coroutinesCore)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "io.github.tarifchakder.networkobserver"
    compileSdk = libs.versions.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.tarifchakder.networkobserver",
        artifactId = "networkobserver",
        version = "1.0.0"
    )

    pom {
        name.set("NetworkObserver")
        description.set("\uD83C\uDF10 NetworkObserver – A Lightweight Kotlin Multiplatform Library for Real-Time Network Updates")
        inceptionYear.set("2025")
        url.set("https://github.com/tarifchakder/NetworkObserver")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("tarif")
                name.set("tarif")
                email.set("mretchcoder@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/tarifchakder/NetworkObserver")
        }
    }
    publishToMavenCentral(true)
    signAllPublications()
}