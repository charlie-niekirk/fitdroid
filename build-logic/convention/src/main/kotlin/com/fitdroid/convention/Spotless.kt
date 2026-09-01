package com.fitdroid.convention

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

internal fun Project.configureSpotless() {
    apply(plugin = "com.diffplug.spotless")
    extensions.configure<SpotlessExtension> {
        val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
        val composeRulesVersion = libs.findVersion("composeRules").get().requiredVersion
        kotlin {
            target("src/**/*.kt")
            ktlint(ktlintVersion)
                .editorConfigOverride(ktlintEditorConfigOverride)
                .customRuleSets(
                    listOf("io.nlopez.compose.rules:ktlint:$composeRulesVersion"),
                )
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(ktlintVersion).editorConfigOverride(ktlintEditorConfigOverride)
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("xml") {
            target("src/**/*.xml")
            leadingTabsToSpaces(4)
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

internal val ktlintEditorConfigOverride: Map<String, String> = mapOf(
    "ktlint_code_style" to "android_studio",
    "ktlint_function_naming_ignore_when_annotated_with" to "Composable, Test",
    "ktlint_standard_property-naming" to "disabled",
    "ij_kotlin_allow_trailing_comma" to "true",
    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
    "max_line_length" to "120",
)
