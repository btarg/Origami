import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}


repositories {
    mavenCentral()
    google()
    maven("https://repo.papermc.io/repository/maven-public/")

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

}

dependencies {
    api("commons-io:commons-io:2.13.0")
    implementation("de.tr7zw:item-nbt-api:2.11.3")
    api("org.projectlombok:lombok:1.18.28")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

}

group = "io.github"
version = "1.0-SNAPSHOT"
description = "Origami"
java.sourceCompatibility = JavaVersion.VERSION_17

tasks {
    named<ShadowJar>("shadowJar") {

        relocate("de.tr7zw.changeme", "thirdparty")
    }
}

tasks {
    register("copyJar") {
        dependsOn(shadowJar)
        doLast {
            val outputDir = "E:/MINECRAFT TEST SERVER/plugins"
            val outputJarName = "${project.name}-${project.version}-shaded.jar"

            val outputJarFile = File(outputDir, outputJarName)
            val shadowJarFile = shadowJar.get().archiveFile.get().asFile

            outputJarFile.parentFile.mkdirs()
            shadowJarFile.copyTo(outputJarFile, true)

            println("Shaded JAR copied to: $outputJarFile")
        }
    }
}

fun executeCopyJarTask(project: Project) {
    val copyJarTask = project.tasks.getByName("copyJar")
    copyJarTask.actions.forEach { action -> action.execute(copyJarTask) }
}

tasks {
    assemble {
        dependsOn(shadowJar)
        doLast {
            executeCopyJarTask(project)
        }
    }
}
