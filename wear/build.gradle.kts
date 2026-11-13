import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val ciStoreFile = System.getenv("SIGNING_STORE_FILE")
val ciStorePassword = System.getenv("SIGNING_STORE_PASSWORD")
val ciKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
val ciKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")

val storeFilePath = ciStoreFile ?: localProperties.getProperty("RELEASE_STORE_FILE", "")
val storeF: File? = if (storeFilePath.isNotEmpty()) file(storeFilePath) else null
val storeP: String = ciStorePassword ?: localProperties.getProperty("RELEASE_STORE_PASSWORD", "")
val keyA: String = ciKeyAlias ?: localProperties.getProperty("RELEASE_KEY_ALIAS", "")
val keyP: String = ciKeyPassword ?: localProperties.getProperty("RELEASE_KEY_PASSWORD", "")

android {
    namespace = "com.diabdata.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.diabdata"
        minSdk = 26
        targetSdk = 36
        versionCode = 15
        versionName = "1.7"

    }

    if (storeF != null && storeF.exists()) {
        signingConfigs {
            create("release") {
                storeFile = storeF
                storePassword = storeP
                keyAlias = keyA
                keyPassword = keyP
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (storeF != null && storeF.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Platform BOMs for consistent versions
    implementation(platform(libs.androidx.compose.bom))

    // Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    // Wear specific
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.horologist.compose.tools)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.wear)

    // Tiles - These are the key changes
    implementation(libs.androidx.tiles.material) // The material components for tiles (now using the single, correct alias)
    implementation(libs.horologist.tiles)

    // Protolayout (correctly declared)
    implementation(libs.androidx.protolayout)
    implementation(libs.androidx.protolayout.material3)
    implementation(libs.androidx.protolayout.expression)

    // Watch Face & Complications
    implementation(libs.androidx.watchface)
    implementation(libs.androidx.watchface.complications.data.source)
    implementation(libs.androidx.watchface.complications.data.source.ktx)

    // Data Layer & Coroutines
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)

    // Other utilities
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(project(":shared"))
    implementation(libs.core.ktx)

    // Debug and Test Implementations
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.renderer)
    debugImplementation(libs.tiles.tooling.preview)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.tiles.testing)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.runner)
}