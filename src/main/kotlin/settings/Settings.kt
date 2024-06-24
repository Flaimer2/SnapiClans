package ru.snapix.clan.settings

import ru.snapix.clan.snapiClan
import ru.snapix.library.config.Configuration
import ru.snapix.library.config.configurationOptions
import ru.snapix.library.config.create

object Settings {
    private val options = configurationOptions {
        serializers += ClanRoleSerializer()
        createSingleElementCollections = true
    }
    private val mainConfig = Configuration.create("config.yml", MainConfig::class.java, snapiClan, options)
    private val messageConfig = Configuration.create("message.yml", MessageConfig::class.java, snapiClan, options)
    private val databaseConfig = Configuration.create("database.yml", DatabaseConfig::class.java, snapiClan, options)
    val config = mainConfig.data()
    val message = messageConfig.data()
    val database = databaseConfig.data()

    fun reload() {
        mainConfig.reloadConfig()
        messageConfig.reloadConfig()
        databaseConfig.reloadConfig()
    }
}
