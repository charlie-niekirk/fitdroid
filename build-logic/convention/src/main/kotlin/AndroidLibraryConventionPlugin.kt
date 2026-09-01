import com.android.build.api.dsl.LibraryExtension
import com.fitdroid.convention.configureAndroidDefaults
import com.fitdroid.convention.configureDetekt
import com.fitdroid.convention.configureKotlinAndroid
import com.fitdroid.convention.configureSpotless
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            extensions.configure<LibraryExtension> {
                configureAndroidDefaults()
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    // AppAuth's manifest requires this placeholder. Application modules
                    // override it; library androidTest APKs need a default to merge.
                    manifestPlaceholders["appAuthRedirectScheme"] = "com.fitdroid"
                }
                testOptions.animationsDisabled = true
                resourcePrefix = path
                    .split("""\W""".toRegex())
                    .drop(1)
                    .distinct()
                    .joinToString(separator = "_")
                    .lowercase() + "_"
            }
            configureKotlinAndroid()
            configureSpotless()
            configureDetekt()
        }
    }
}
