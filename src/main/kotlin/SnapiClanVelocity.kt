package ru.snapix.clan

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import ru.snapix.clan.caches.Clans
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.library.velocity.VelocityPlugin
import java.nio.file.Path

@Plugin(
    id = "snapiclans",
    name = "SnapiClans",
    version = "1.3",
    authors = ["SnapiX"],
    dependencies = [Dependency(id = "snapilibrary")]
)
class SnapiClanVelocity @Inject constructor(server: ProxyServer, logger: Logger, @DataDirectory dataDirectory: Path) : VelocityPlugin() {
    init {
        init(server, logger, dataDirectory)
    }

    @Subscribe
    fun onEnable(event: ProxyInitializeEvent) {
        ClanDatabase.getClans().forEach { Clans.updateClan(it) }
        ClanDatabase.getUsers().forEach { Clans.updateUser(it) }
    }
}
