// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.aboutLibraries) apply false

}

tasks.register("generateTestData") {
    description = "Generates a test ZIP file with fake DiabData data"
    group = "diabdata"
    doLast {
        val kotlinPath = listOf(
            // Homebrew (macOS Apple Silicon)
            "/opt/homebrew/bin/kotlin",
            // Homebrew (macOS Intel)
            "/usr/local/bin/kotlin",
            // SDKMAN
            "${System.getProperty("user.home")}/.sdkman/candidates/kotlin/current/bin/kotlin",
            // Linux snap
            "/snap/bin/kotlin",
            // Android Studio embedded Kotlin
            "/Applications/Android Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlin",
            // Windows (common)
            "C:\\Program Files\\kotlinc\\bin\\kotlin.bat"
        ).firstOrNull { File(it).exists() }
            ?: throw GradleException(
                """
                ❌ Kotlin CLI not found. Install it via one of:
                  • macOS:   brew install kotlin
                  • Linux:   sdk install kotlin
                  • Manual:  https://kotlinlang.org/docs/command-line.html
                """.trimIndent()
            )

        println("🔧 Using Kotlin: $kotlinPath")

        val result = providers.exec {
            workingDir = rootDir
            commandLine(kotlinPath, "scripts/generateTestData.kts")
        }
        println(result.standardOutput.asText.get())
    }
}
