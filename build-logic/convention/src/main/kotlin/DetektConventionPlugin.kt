import com.rustor.buildlogic.libs
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("dev.detekt")

            extensions.configure<DetektExtension> {
                config.setFrom(rootProject.files("config/detekt/detekt.yml"))
                buildUponDefaultConfig.set(true)
                parallel.set(true)
                autoCorrect.set(true)
            }

            dependencies {
                "detektPlugins"(libs.findLibrary("detekt-ktlint").get())
            }

            tasks.withType<Detekt>().configureEach {
                reports {
                    html.required.set(true)
                    sarif.required.set(true)
                }
            }
        }
    }
}