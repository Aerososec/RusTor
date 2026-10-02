import com.rustor.buildlogic.libs
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.gitlab.arturbosch.detekt")

       extensions.configure<DetektExtension> {
           config.setFrom(rootProject.files("config/detekt/detekt.yml"))
           buildUponDefaultConfig = true
           autoCorrect = true
           parallel = true
       }

        dependencies {
            "detektPlugins"(libs.findLibrary("detekt-formatting").get())
        }

        tasks.withType<Detekt>().configureEach {
            jvmTarget = "17"
            reports {
                html.required.set(true)
                xml.required.set(false)
                txt.required.set(false)
                sarif.required.set(true)
                md.required.set(false)
            }
        }
    }
}