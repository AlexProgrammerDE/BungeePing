plugins {
    base
}

allprojects {
    group = "net.pistonmaster"
    version = "4.4.0"
    description = "Best MOTD plugin with multi-platform support!"
}

val platforms = setOf(
    projects.pistonmotdBukkit,
    projects.pistonmotdBungee,
    projects.pistonmotdSponge,
    projects.pistonmotdVelocity
).map { it.dependencyProject }

val shadow = setOf(
    projects.pistonmotdUtils
).map { it.dependencyProject }

val special = setOf(
    projects.pistonmotdUniversal,
    projects.pistonmotdApi
).map { it.dependencyProject }

subprojects {
    when (this) {
        in platforms -> plugins.apply("pm.platform-conventions")
        in shadow -> plugins.apply("pm.shadow-conventions")
        in special -> plugins.apply("pm.java-conventions")
    }
}