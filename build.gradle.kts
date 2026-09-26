plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "dev.hadimhz.welcome"
version = ""

repositories {
    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.1-R0.1-SNAPSHOT")

    implementation(files("./libs/config.jar"))

}