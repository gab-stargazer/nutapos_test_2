pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            setUrl("https://jitpack.io")
        }
        mavenCentral()
    }
}

rootProject.name = "Nutapos Test 2"
include(":app")
 