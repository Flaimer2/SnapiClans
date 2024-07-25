plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.plugin.yml.bukkit)
    alias(libs.plugins.serialization)
}

group = "ru.snapix"
version = "3.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.bukkit)
    compileOnly(libs.snapilibrary)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.serialization)
    compileOnly(files("libs/AlonsoLevels.jar"))
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-XDenableSunApiLintControl"))
}

tasks.jar {
    archiveFileName.set("${project.name}.jar")
}

bukkit {
    main = "ru.snapix.snapiclans.SnapiClan"
    author = "SnapiX"
    website = "https://mcsnapix.ru"
    depend = listOf("SnapiLibrary")
}