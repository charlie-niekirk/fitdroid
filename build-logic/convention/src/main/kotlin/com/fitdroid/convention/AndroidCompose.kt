package com.fitdroid.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(extension: ApplicationExtension) {
    extension.buildFeatures.compose = true
    addComposeDependencies()
}

internal fun Project.configureAndroidCompose(extension: LibraryExtension) {
    extension.buildFeatures.compose = true
    addComposeDependencies()
}

private fun Project.addComposeDependencies() {
    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        "implementation"(platform(bom))
        "androidTestImplementation"(platform(bom))
        "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}
