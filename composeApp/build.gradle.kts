
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

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
            isStatic = false // todo: try this with true when the issue with SQLDelight is solved

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
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.android.driver)
            implementation(libs.accompanist.permissions)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.sqlite.driver)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.material3)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodel)

            implementation(libs.kotlinInject.runtime)

            implementation("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.3.1")

            val napier = "2.7.1"
            implementation("io.github.aakira:napier:$napier")

            val coil = "3.0.0-alpha01"
            implementation("io.coil-kt.coil3:coil:$coil")
            implementation("io.coil-kt.coil3:coil-compose-core:$coil")

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

            implementation(libs.coroutines.extensions)

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
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
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
    implementation(libs.androidx.lifecycle.viewmodel.compose)// KSP will eventually have better multiplatform support and we'll be able to simply have
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

    implementation(libs.kotlinInject.runtime)
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