plugins {
    alias(libs.plugins.rustor.android.library.compose)
}

android {
    namespace = "com.rustor.core.ui"
}

dependencies {
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}


