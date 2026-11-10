plugins {
    alias(libs.plugins.rustor.android.application)
    alias(libs.plugins.rustor.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rustor"

    defaultConfig {
        applicationId = "com.rustor"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":data:catalog"))
    implementation(project(":feature:showcase"))
    implementation(project(":feature:categories"))
    implementation(project(":feature:appdetails"))
}