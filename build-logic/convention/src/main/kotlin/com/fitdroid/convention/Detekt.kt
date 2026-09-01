package com.fitdroid.convention

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt() {
    apply(plugin = "dev.detekt")
    extensions.configure<DetektExtension> {
        toolVersion.set(libs.findVersion("detekt").get().requiredVersion)
        buildUponDefaultConfig.set(true)
        allRules.set(false)
        parallel.set(true)
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        basePath.set(rootProject.layout.projectDirectory)
        ignoredBuildTypes.set(listOf("release"))
    }
    tasks.withType<Detekt>().configureEach {
        jvmTarget.set("17")
        exclude("**/build/**")
        reports {
            html.required.set(true)
            sarif.required.set(true)
            markdown.required.set(true)
        }
    }
    dependencies {
        "detektPlugins"(libs.findLibrary("detekt-compose-rules").get())
    }
}
