package ru.snapix.clan

import org.bukkit.plugin.java.JavaPlugin
import ru.snapix.clan.caches.Clans
import ru.snapix.clan.caches.Users
import ru.snapix.clan.commands.Commands
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.clan.listeners.ClanListener
import ru.snapix.library.network.ServerType

class SnapiClan : JavaPlugin() {
    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        ClanDatabase.load()
        Commands.enable()

        if (ru.snapix.library.bukkit.plugin.serverType == ServerType.LOBBY) {
            ClanDatabase.clans().forEach { Clans.update(it) }
            ClanDatabase.users().forEach { Users.update(it) }
        }

        server.pluginManager.registerEvents(ClanListener(), this)
        ClanExpansion().register()
    }

    companion object {
        lateinit var instance: SnapiClan
    }
}

val plugin = SnapiClan.instance
const val KEY_REDIS_INVITE = "clan-invite"