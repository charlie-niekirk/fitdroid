import com.android.build.api.dsl.ApplicationExtension
import com.fitdroid.convention.configureAndroidDefaults
import com.fitdroid.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            extensions.configure<ApplicationExtension> {
                configureAndroidDefaults()
                testOptions.animationsDisabled = true
            }
            configureKotlinAndroid()
        }
    }
}
