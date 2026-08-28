import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class MetroConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "dev.zacsweers.metro")
    }
}
