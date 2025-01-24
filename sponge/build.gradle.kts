import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("pm.platform-conventions")
    id("pm.shadow-conventions")
    id("org.spongepowered.gradle.plugin") version "2.3.0"
}

dependencies {
    implementation(project(":pistonmotd-api", "shadow"))
    implementation(projects.pistonmotdShared)
    compileOnly(projects.pistonmotdBuildData)

    implementation("org.bstats:bstats-sponge:3.1.0")
}

sponge {
    injectRepositories(false)
    apiVersion("8.0.0")
    license("Apache License 2.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("pistonmotd") {
        displayName("PistonMOTD")
        entrypoint("net.pistonmaster.pistonmotd.sponge.PistonMOTDSponge")
        description(rootProject.description)
        links {
            homepage("https://pistonmaster.net/PistonMOTD")
            source("https://github.com/AlexProgrammerDE/PistonMOTD")
            issues("https://github.com/AlexProgrammerDE/PistonMOTD/issues")
        }
        contributor("AlexProgrammerDE") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder.set(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}
