plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.+"
    id("io.papermc.paperweight.userdev") version "1.+"
    id("xyz.jpenilla.run-paper") version "2.+" // Adds runServer and runMojangMappedServer tasks for testing
    id("io.freefair.lombok") version "8.+"
}
repositories {
    mavenCentral()
    google()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}
dependencies {
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
    api("commons-io:commons-io:2.+")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

}
group = "io.github"
version = "1.0-SNAPSHOT"
description = "Origami"
java.sourceCompatibility = JavaVersion.VERSION_17
tasks {
    assemble {
        dependsOn(reobfJar)
        dependsOn(shadowJar)
    }
    runServer {
        dependsOn(assemble)
        minecraftVersion("1.20.2")
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
                "version" to project.version
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

}
