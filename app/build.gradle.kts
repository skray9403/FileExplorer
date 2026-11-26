import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val signingProps = Properties().apply {
    val f = rootProject.file("signing.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.explorer.fileexplorer"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.explorer.fileexplorer"
        minSdk = 26
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 22
        versionName = "1.6.2-preview"
    }

    val releaseStorePassword = signingProps.getProperty("STORE_PASSWORD")
        ?: System.getenv("FILEEXPLORER_STORE_PASSWORD")
    val releaseKeyPassword = signingProps.getProperty("KEY_PASSWORD")
        ?: System.getenv("FILEEXPLORER_KEY_PASSWORD")
    val releaseStoreFile = file("${rootProject.projectDir}/${signingProps.getProperty("STORE_FILE", "fileexplorer.jks")}")
    val hasReleaseSigning = releaseStorePassword != null && releaseKeyPassword != null && releaseStoreFile.isFile

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = signingProps.getProperty("KEY_ALIAS", "fileexplorer")
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    lint {
        disable.add("MissingTranslation")
        abortOnError = false
    }
    buildFeatures { compose = true }
    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    implementation(project(":core:designsystem"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    testImplementation(kotlin("test"))
}
