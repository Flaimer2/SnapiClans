package ru.snapix.clan.settings

import ru.snapix.clan.snapiClan
import ru.snapix.library.Configuration
import ru.snapix.library.create
import space.arim.dazzleconf.ConfigurationOptions

object Settings {
    private val options = ConfigurationOptions.Builder()
        .addSerialiser(ClanRoleSerializer())
        .setCreateSingleElementCollections(true)
        .build()
    private val mainConfig = Configuration.create("config.yml", MainConfig::class.java, snapiClan)
    private val messageConfig = Configuration.create("message.yml", MessageConfig::class.java, snapiClan)
    private val databaseConfig = Configuration.create("database.yml", DatabaseConfig::class.java, snapiClan)
    val config = mainConfig.data()
    val message = messageConfig.data()
    val database = databaseConfig.data()

}