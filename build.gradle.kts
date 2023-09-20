plugins {
    id("java")
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.superpenguin"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://www.jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    implementation("com.github.SuperGlueLib:SuperFoundations:1.2.0")
    implementation("com.github.SuperGlueLib:LampWrapper:31f785c2d7")
}

kotlin {
    jvmToolchain(17)
}

tasks.processResources {
    val props = LinkedHashMap(mapOf("version" to version))
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand (props)
    }
}
