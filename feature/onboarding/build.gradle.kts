plugins {
    alias(libs.plugins.rustor.android.feature)
}

android {
    namespace = "com.rustor.feature.onboarding"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies{
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.compose.foundation)
}