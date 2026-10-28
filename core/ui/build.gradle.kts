plugins {
    alias(libs.plugins.rustor.android.library.compose)
}

android {
    namespace = "com.rustor.core.ui"
}

dependencies {
    implementation(libs.androidx.compose.ui.text.google.fonts)
}


