plugins {
    id("pm.kyori-conventions")
}

dependencies {
    implementation(projects.pistonmotdApi)
    implementation(projects.pistonmotdShared)
    implementation(projects.pistonmotdKyoriRelocated)

    implementation("org.bstats:bstats-bungeecord:2.2.1")
    implementation("net.kyori:adventure-platform-bungeecord:4.0.1")

    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")
}
