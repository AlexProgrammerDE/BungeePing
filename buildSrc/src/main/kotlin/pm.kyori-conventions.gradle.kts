import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("pm.shadow-conventions")
}

dependencies {
    implementation("net.kyori:adventure-api:4.9.3")
    implementation("net.kyori:adventure-text-serializer-legacy:4.9.3")
}

tasks.named<ShadowJar>("shadowJar").get().apply {
    relocate("net.kyori", "net.pistonmaster.pistonmotd.shadow.kyori")
}