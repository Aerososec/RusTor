plugins {
    alias(libs.plugins.rustor.android.feature)
}

android {
    namespace = "com.rustor.feature.showcase"
}

dependencies{
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
}