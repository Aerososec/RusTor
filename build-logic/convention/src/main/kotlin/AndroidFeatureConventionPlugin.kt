import com.android.build.gradle.LibraryExtension
import com.rustor.buildlogic.configureAndroidCompose
import com.rustor.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("moodreel.android.library")
            pluginManager.apply("moodreel.android.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this)
            }

            dependencies {
                "implementation"(libs.findBundle("lifecycle").get())
                "implementation"(libs.findBundle("coroutines").get())
                "implementation"(libs.findLibrary("hilt-navigation-compose").get())

                "testImplementation"(libs.findBundle("testing-unit").get())
                "androidTestImplementation"(libs.findBundle("testing-compose").get())
            }
        }
    }
}