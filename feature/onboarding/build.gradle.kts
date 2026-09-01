plugins {
    alias(libs.plugins.fitdroid.android.feature)
}

android {
    namespace = "com.fitdroid.feature.onboarding"
}

dependencies {
    implementation(projects.core.health)
    implementation(projects.core.auth)
    implementation(projects.core.network)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.orbit.test)
}
