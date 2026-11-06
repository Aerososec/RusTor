plugins {
    alias(libs.plugins.rustor.android.feature)
}

android {
    namespace = "com.rustor.feature.categories"
}

dependencies{
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
}