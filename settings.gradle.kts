pluginManagement {

    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RusTor"
include(":app")

//core
include(":core:domain")
include(":core:common")
include(":core:model")
include(":core:database")

//feature
include(":feature:onboarding")
include(":feature:showcase")

//data
include(":data:catalog")
