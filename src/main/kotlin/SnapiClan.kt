package ru.snapix.clan

import org.bukkit.plugin.java.JavaPlugin
import ru.snapix.clan.caches.Clans
import ru.snapix.clan.commands.Commands
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.clan.listeners.ChatListener
import ru.snapix.clan.messenger.Messenger
import ru.snapix.clan.settings.Settings

class SnapiClan : JavaPlugin() {
    override fun onLoad() {
        instance = this
        Settings
    }

    override fun onEnable() {
        ClanDatabase.load()
        Commands.enable()
        Messenger.enable()

        if (Settings.config.isLobby()) {
            ClanDatabase.clans().forEach { Clans.updateClan(it) }
            ClanDatabase.users().forEach { Clans.updateUser(it) }
        }

        server.pluginManager.registerEvents(ChatListener(), this)
    }

    companion object {
        @JvmStatic
        lateinit var instance: SnapiClan
            private set
    }
}

val snapiClan = SnapiClan.instance
const val KEY_REDIS_MESSENGER = "snapiclan"
const val KEY_REDIS_INVITE = "clan_invite"