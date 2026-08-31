plugins {
    alias(libs.plugins.fitdroid.android.library)
    alias(libs.plugins.fitdroid.metro)
}

android {
    namespace = "com.fitdroid.core.health"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    implementation(libs.androidx.health.connect.client)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.health.connect.testing)
}
