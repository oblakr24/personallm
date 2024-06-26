
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.sqldelight)
}

repositories {
    google()
    mavenCentral()
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("personallm.db")
            generateAsync.set(true)
        }
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries {
            findTest(NativeBuildType.DEBUG)?.linkerOpts("-lsqlite3")
        }
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false // todo: try this with true when the issue with SQLDelight is solved: https://github.com/cashapp/sqldelight/issues/5007

            export(libs.arkivanov.decompose)
            export(libs.arkivanov.essenty.lifecycle)
            // Optional, only if you need state preservation on Darwin (Apple) targets
            export(libs.arkivanov.essenty.state.keeper)

            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            transitiveExport = true
        }
    }
    
    sourceSets {
        val desktopMain by getting

        iosMain.dependencies {
            api(libs.arkivanov.decompose)
            api(libs.arkivanov.essenty.lifecycle)
            implementation(libs.native.driver)
            implementation(libs.kotlinInject.runtime.kmp)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.android.driver)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.sqlite.driver)
            implementation(libs.kotlinInject.runtime.kmp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.material3)
            implementation(compose.components.uiToolingPreview)

            // CMP resources
            implementation(compose.components.resources)

            // Multiplatform settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            implementation(libs.constraintlayout.compose.multiplatform)

            // Napier
            implementation(libs.napier)

            // Coil
            implementation(libs.coil)
            implementation(libs.coil.compose.core)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            runtimeOnly(libs.kotlinx.coroutines.swing)
            implementation(libs.essenty.lifecycle.coroutines)

            implementation(libs.molecule.runtime)

            implementation(compose.materialIconsExtended)

            // DataStore
            implementation(libs.androidx.datastore.preferences.core)

            // Decompose
            api(libs.arkivanov.decompose)
            implementation(libs.arkivanov.decompose.extensions)


            implementation(libs.coroutines.extensions.sqldelight)

            implementation(libs.kotlinx.serialization.json)

            // Ktor
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.datetime)
            implementation(libs.ktorfit.lib)
        }
    }
}

android {
    namespace = "com.rokoblak.personallm"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.rokoblak.personallm"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            val prop = Properties().apply {
                load(FileInputStream(File(rootProject.rootDir, "local.properties")))
            }
            val storePass = prop.getProperty("STORE_PASS", null)
            if (storePass == null) {
                println("No store pass, please ensure you have it in local.properties")
                return@create
            }
            val keyPass = prop.getProperty("KEY_PASS", null)
            if (keyPass == null) {
                println("No key pass, please ensure you have it in local.properties")
                return@create
            }
            storeFile = file("personallm.jks")
            storePassword = storePass
            keyAlias = "personallm"
            keyPassword = keyPass
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

dependencies {
    implementation(libs.androidx.ui.tooling.preview.desktop)
    // `ksp libs.kotlinInject.compiler` in the dependencies block of each source set
    // https://github.com/google/ksp/pull/1021
    with(libs.kotlinInject.compiler) {
        add("kspIosX64", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }

    with(libs.sqlite.driver) {
        add("kspIosX64", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
        add("kspIosX64", this)
        add("kspIosX64Test", this)
        add("kspIosArm64", this)
        add("kspIosArm64Test", this)
        add("kspIosSimulatorArm64", this)
        add("kspIosSimulatorArm64Test", this)
    }

    with(libs.ktorfit.ksp) {
        add("kspCommonMainMetadata", this)
        add("kspDesktop", this)
        add("kspAndroid", this)
        add("kspAndroidTest", this)
        add("kspIosX64", this)
        add("kspIosX64Test", this)
        add("kspIosArm64", this)
        add("kspIosArm64Test", this)
        add("kspIosSimulatorArm64", this)
        add("kspIosSimulatorArm64Test", this)
    }

    implementation(libs.kotlinInject.runtime.kmp)
    ksp(libs.kotlinInject.compiler)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.rokoblak.personallm"
            packageVersion = "1.0.0"
        }
    }
}

fun loadProperties(): Properties {
    val props = Properties()
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use {
            props.load(it)
        }
    }
    return props
}

val localProps = loadProperties()
val OPENAI_KEY_NAME = "OPENAI_API_KEY"
val ANTHROPIC_KEY_NAME = "ANTHROPIC_API_KEY"
val openAiKey: String = localProps.getProperty(OPENAI_KEY_NAME, "")
val anthropicAIKey: String = localProps.getProperty(ANTHROPIC_KEY_NAME, "")

tasks.register("generateBuildConfig") {
    if (openAiKey.isBlank()) {
        println("$OPENAI_KEY_NAME is missing from local.properties")
    }
    if (anthropicAIKey.isBlank()) {
        println("$ANTHROPIC_KEY_NAME is missing from local.properties")
    }

    doLast {
        val fileContent = """
            package com.rokoblak.personallm.config
            
            // Autogenerated, do not modify
            object AppBuildConfig {
                const val $OPENAI_KEY_NAME = "$openAiKey"
                const val $ANTHROPIC_KEY_NAME = "$openAiKey"
            }
        """.trimIndent()

        val buildDir = layout.buildDirectory.get().asFile
        val commonMainDir = file("${buildDir}/generated/kotlin/config/")
        commonMainDir.mkdirs()
        val generatedFile = commonMainDir.resolve("AppBuildConfig.kt")
        generatedFile.delete()
        generatedFile.writeText(fileContent)
        val success = generatedFile.setReadOnly()
        if (!success) {
            logger.warn("Failed to set the file as read-only: ${generatedFile.absolutePath}")
        }
    }
}

kotlin.sourceSets.getByName("commonMain") {
    val buildDir = layout.buildDirectory.get().asFile
    kotlin.srcDir("${buildDir}/generated/kotlin/config")
}

kotlin.targets.all {
    compilations.all {
        compileTaskProvider.dependsOn("generateBuildConfig")
    }
}