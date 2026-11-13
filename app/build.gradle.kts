import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.aboutLibraries)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val ciStoreFile: String? = System.getenv("SIGNING_STORE_FILE")
val ciStorePassword: String? = System.getenv("SIGNING_STORE_PASSWORD")
val ciKeyAlias: String? = System.getenv("SIGNING_KEY_ALIAS")
val ciKeyPassword: String? = System.getenv("SIGNING_KEY_PASSWORD")

val storeFilePath: String = ciStoreFile ?: localProperties.getProperty("RELEASE_STORE_FILE", "")
val storeF: File? = if (storeFilePath.isNotEmpty()) file(storeFilePath) else null
val storeP: String = ciStorePassword ?: localProperties.getProperty("RELEASE_STORE_PASSWORD", "")
val keyA: String = ciKeyAlias ?: localProperties.getProperty("RELEASE_KEY_ALIAS", "")
val keyP: String = ciKeyPassword ?: localProperties.getProperty("RELEASE_KEY_PASSWORD", "")

android {
    namespace = "com.diabdata"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.diabdata"
        testApplicationId = "com.diabdata.test"
        minSdk = 26
        targetSdk = 36
        versionCode = 15
        versionName = "4.9.8"
        buildConfigField("String", "RELAY_SERVER_URL","\"${localProperties.getProperty("RELAY_SERVER_URL", "")}\"")
        buildConfigField("String", "MEDICATION_GTIN_FILE_VERSION", "\"1.2.0\"")
        buildConfigField("String", "MEDICAL_DEVICES_GTIN_FILE_VERSION", "\"1.0.4\"")
        buildConfigField("String", "APP_VARIANT", "\"${localProperties.getProperty("APP_VARIANT", "development")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    sourceSets {
        getByName("androidTest") {
            assets.directories.add("$projectDir/schemas")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/MANIFEST.MF",
                "META-INF/io.netty.versions.properties",
                "META-INF/license/**",
                "META-INF/native-image/**"
            )
        }
    }

    buildToolsVersion = "36.0.0"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

aboutLibraries {
    collect {
        configPath = file("config")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)

    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // BOM
    @Suppress("AvoidDuplicateDependencies")
    // Suppressed because we need it both for the implementation and tests
    implementation(composeBom)

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text)

    // Image loading
    implementation(libs.coil.compose)

    // Vico
    implementation(libs.vico.compose)
    // implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    // implementation(libs.vico.multiplatform)
    // implementation(libs.vico.views)

    // Coroutines
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.androidx.navigation.compose)

    // Material - CHOOSE ONLY ONE
    // implementation(libs.androidx.material3)           // Stable version
    // Or expressive alpha version :
    implementation(libs.material3.expressive)      // Unstable expressive version
    implementation(libs.material)
    implementation(libs.androidx.material.icons.extended)

    // Adaptive compose libs
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)

    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.animation)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.work.runtime.ktx)

    // Encryption
    implementation(libs.sqlcipher.android)
    implementation(libs.zip4j)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    androidTestImplementation(libs.androidx.room.testing)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Widgets
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.lifecycle.process)

    // Wear OS complication
    implementation(libs.androidx.watchface.complications.data.source)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)

    // Annotation processing
    ksp(libs.androidx.room.compiler)
    implementation(libs.gson)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ZXing
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    @Suppress("AvoidDuplicateDependencies")
    // Suppressed because we need it both for the implementation and tests
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // KTOR SERVER ON DEVICE [WILL BE DEPRECATED AFTER MIGRATION TO RELAY SERVER]
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)

    // KTOR CLIENT WEBSOCKET [FOR THE NEW RELAY SERVER SYSTEM]
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Ktor + GSON
    implementation(libs.ktor.serialization.gson)

    // Licenses
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)

    // Shared
    implementation(project(":shared"))
}