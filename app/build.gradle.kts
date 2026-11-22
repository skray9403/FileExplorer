import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val signingProps = Properties().apply {
    val f = rootProject.file("signing.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.explorer.fileexplorer"
    compileSdk = 37
    compileSdkMinor = 1

    defaultConfig {
        applicationId = "com.explorer.fileexplorer"
        minSdk = 26
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 22
        versionName = "1.6.2"
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
    implementation(project(":core:model"))
    implementation(project(":core:plugin"))
    implementation(project(":core:data"))
    implementation(project(":core:storage"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:browser"))
    implementation(project(":feature:transfer"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:search"))
    implementation(project(":core:network"))
    implementation(project(":core:cloud"))
    implementation(project(":feature:network"))
    implementation(project(":feature:cloud"))
    implementation(project(":feature:security"))
    implementation(project(":feature:editor"))
    implementation(project(":feature:apps"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.coil.compose)

    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(kotlin("test"))
}
