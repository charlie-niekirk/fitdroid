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
    kotlin {
        target("build-logic/convention/src/**/*.kt")
        ktlint(ktlintVersion)
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        target("build-logic/*.gradle.kts")
        target("build-logic/convention/*.gradle.kts")
        ktlint(ktlintVersion)
        trimTrailingWhitespace()
        endWithNewline()
    }
}
