enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("com.gradleup.shadow") version "9.0.0-beta6"
        id("org.cadixdev.licenser") version "0.6.1"
        id("net.kyori.indra") version "3.1.3"
        id("net.kyori.indra.git") version "3.1.3"
        id("net.kyori.indra.publishing") version "3.1.3"
        id("net.kyori.blossom") version "2.1.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            name = "Sonatype"
        }
        maven("https://papermc.io/repo/repository/maven-public/") {
            name = "PaperMC"
        }
        maven("https://repo.spongepowered.org/maven") {
            name = "SpongePowered"
        }
        maven("https://nexus.velocitypowered.com/repository/maven-public/") {
            name = "VelocityPowered"
        }
        maven("https://repo.codemc.org/repository/maven-public") {
            name = "CodeMC"
        }
        maven("https://jitpack.io") {
            name = "jitpack.io"
        }
    }
}

rootProject.name = "PistonMOTD"

setOf(
    "build-data",
    "kyori",
    "api",
    "shared",
    "bukkit",
    "bungee",
    "sponge",
    "velocity",
    "universal"
).forEach { setupPMSubproject(it) }

fun setupPMSubproject(name: String) {
    setupSubproject("pistonmotd-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
