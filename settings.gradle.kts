pluginManagement {
    repositories {
        gradlePluginPortal()
        // DO NOT REMOVE, NEEDED FOR FLATHUB OFFLINE BUILD (!= to mavenLocal)
        maven {
            url = uri("./offline-repository")
        }
    }
}

rootProject.name = "LEDSuite"