plugins {
    alias(libs.plugins.rustor.android.library.compose)
}

android {
    namespace = "com.rustor.core.ui"
}

dependencies {
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}


