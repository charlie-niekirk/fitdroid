plugins {
    alias(libs.plugins.fitdroid.android.library)
    alias(libs.plugins.fitdroid.metro)
}

android {
    namespace = "com.fitdroid.core.auth"
}

dependencies {
    api(projects.core.common)
    implementation(libs.appauth)
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
