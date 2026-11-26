pluginManagement {
    repositories {
        google {
            content {
                includeGroup("com.android")
                includeGroupByRegex("com\\.android\\..*")
                includeGroupByRegex("com\\.google\\..*")
                includeGroupByRegex("androidx\\..*")
                includeGroup("org.jetbrains.kotlin")
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
        mavenCentral {
            content {
                excludeGroup("com.github.topjohnwu.libsu")
                excludeGroup("com.github.thegrizzlylabs")
            }
        }
        exclusiveContent {
            forRepository {
                maven { url = uri("https://jitpack.io") }
            }
            filter {
                includeGroup("com.github.topjohnwu.libsu")
                includeGroup("com.github.thegrizzlylabs")
            }
        }
    }
}

rootProject.name = "FileExplorer"

include(":app")
include(":core:designsystem")
