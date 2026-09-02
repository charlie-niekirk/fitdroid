plugins {
    alias(libs.plugins.fitdroid.android.feature)
}

android {
    namespace = "com.fitdroid.feature.settings"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.sync)
    implementation(projects.core.auth)
    implementation(projects.core.network)
    implementation(projects.core.health)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.orbit.test)
}
