plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnlyApi("org.apiguardian:apiguardian-api:1.1.2")
    implementation("net.pistonmaster:PistonUtils:1.3.2")

    compileOnly("net.luckperms:api:5.4")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")
}

tasks {
    processResources {
        expand("version" to version, "description" to description, "url" to "https://pistonmaster.net/PistonMOTD")
    }
    test {
        reports.junitXml.required = true
        reports.html.required = true
        useJUnitPlatform()
        maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).coerceAtLeast(1)
    }
    jar {
        from(rootProject.file("LICENSE"))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all,-serial,-processing")
}

tasks.withType<Javadoc> {
    enabled = false
}

val repoName = if (version.toString().endsWith("SNAPSHOT")) "maven-snapshots" else "maven-releases"
publishing {
    repositories {
        maven("https://repo.codemc.org/repository/${repoName}/") {
            credentials.username = System.getenv("CODEMC_USERNAME")
            credentials.password = System.getenv("CODEMC_PASSWORD")
            name = "codemc"
        }
    }
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = "PistonMOTD"
                description = rootProject.description
                url = "https://github.com/AlexProgrammerDE/PistonMOTD"
                organization {
                    name = "AlexProgrammerDE"
                    url = "https://pistonmaster.net"
                }
                developers {
                    developer {
                        id = "AlexProgrammerDE"
                        timezone = "Europe/Berlin"
                        url = "https://pistonmaster.net"
                    }
                }
                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.html"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/AlexProgrammerDE/PistonMOTD.git"
                    developerConnection = "scm:git:ssh://git@github.com/AlexProgrammerDE/PistonMOTD.git"
                    url = "https://github.com/AlexProgrammerDE/PistonMOTD"
                }
                ciManagement {
                    system = "GitHub Actions"
                    url = "https://github.com/AlexProgrammerDE/PistonMOTD/actions"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/AlexProgrammerDE/PistonMOTD/issues"
                }
            }
        }
    }
}
