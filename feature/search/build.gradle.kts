plugins {
    alias(libs.plugins.rustor.android.feature)
}

android {
    namespace = "com.rustor.feature.search"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.compose.foundation)
}
