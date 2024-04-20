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
        mavenCentral()
        jcenter()
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://jitpack.io") }

// Use uri() function to specify the URL
    }

}


rootProject.name = "Image-To-Text"
include(":app")
 