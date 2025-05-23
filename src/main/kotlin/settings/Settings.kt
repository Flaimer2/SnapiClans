package ru.snapix.clan.settings

import ru.snapix.clan.plugin
import ru.snapix.library.bukkit.settings.create
import ru.snapix.library.settings.Configuration
import ru.snapix.library.settings.configurationOptions

object Settings {
    private val options = configurationOptions {
        serializers += ClanRoleSerializer()
        createSingleElementCollections = true
    }
    private val mainConfig = Configuration.create("config.yml", MainConfig::class.java, plugin, options)
    private val messageConfig = Configuration.create("message.yml", MessageConfig::class.java, plugin, options)
    private val databaseConfig = Configuration.create("database.yml", DatabaseConfig::class.java, plugin, options)
    val config
        get() = mainConfig.data()
    val message
        get() =  messageConfig.data()
    val database
        get() = databaseConfig.data()

    fun reload() {
        mainConfig.reloadConfig()
        messageConfig.reloadConfig()
        databaseConfig.reloadConfig()
    }
}
