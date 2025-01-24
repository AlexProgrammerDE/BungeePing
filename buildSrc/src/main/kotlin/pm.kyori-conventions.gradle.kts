import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("pm.shadow-conventions")
}

dependencies {
    implementation("net.kyori:adventure-api:4.18.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.18.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.18.0")
    implementation("net.kyori:adventure-text-minimessage:4.18.0")
}

tasks.named<ShadowJar>("shadowJar").get().apply {
    relocate("net.kyori", "net.pistonmaster.pistonmotd.shadow.kyori")
}
