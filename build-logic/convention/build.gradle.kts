plugins {
    `kotlin-dsl`
}

group = "com.rustor.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication"){
            id = "rustor.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("detekt") {
            id = "rustor.detekt"
            implementationClass = "DetektConventionPlugin"
        }
        register("jvmLibrary") {
            id = "rustor.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("androidHilt") {
            id = "rustor.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidLibrary") {
            id = "rustor.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidRoom") {
            id = "rustor.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidFeature") {
            id = "rustor.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
    }
}