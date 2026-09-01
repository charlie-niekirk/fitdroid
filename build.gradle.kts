plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt) apply false
}

spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    val ktlintEditorConfigOverride = mapOf(
        "ktlint_code_style" to "android_studio",
        "ktlint_function_naming_ignore_when_annotated_with" to "Composable, Test",
        "ktlint_standard_property-naming" to "disabled",
        "ij_kotlin_allow_trailing_comma" to "true",
        "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
        "max_line_length" to "120",
    )
    kotlin {
        target("build-logic/convention/src/**/*.kt")
        ktlint(ktlintVersion).editorConfigOverride(ktlintEditorConfigOverride)
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        target("build-logic/*.gradle.kts")
        target("build-logic/convention/*.gradle.kts")
        ktlint(ktlintVersion).editorConfigOverride(ktlintEditorConfigOverride)
        trimTrailingWhitespace()
        endWithNewline()
    }
}
