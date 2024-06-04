plugins {
    kotlin("jvm") version "2.0.0"
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
    compileOnly("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("ru.snapix:snapilibrary:1.2-bukkit")
}

bukkit {
    main = "ru.snapix.clan.SnapiClan"
    author = "SnapiX"
    website = "https://mcsnapix.ru"
    depend = listOf("SnapiLibrary")
}