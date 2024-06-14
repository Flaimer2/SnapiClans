plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("kapt") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "ru.snapix"
version = "2.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    kapt("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    compileOnly("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("ru.snapix:snapilibrary-velocity:1.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    compileOnly("ru.snapix:snapilibrary-bukkit:1.3")
}

bukkit {
    main = "ru.snapix.clan.SnapiClan"
    author = "SnapiX"
    website = "https://mcsnapix.ru"
    depend = listOf("SnapiLibrary")
}