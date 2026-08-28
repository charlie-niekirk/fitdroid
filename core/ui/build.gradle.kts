plugins {
    alias(libs.plugins.fitdroid.android.library)
    alias(libs.plugins.fitdroid.android.compose)
    alias(libs.plugins.fitdroid.metro)
}

android {
    namespace = "com.fitdroid.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.designsystem)
    api(libs.metrox.viewmodel)
    api(libs.metrox.viewmodel.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)
}
