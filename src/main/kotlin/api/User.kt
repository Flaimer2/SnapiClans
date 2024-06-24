package ru.snapix.clan.api

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.snapix.clan.ClanRoleSerializer

@Serializable
data class User(val name: String, @Serializable(with = ClanRoleSerializer::class) var role: ClanRole, val clanName: String) {
    fun clan(): Clan? = ClanApi.clan(clanName)

    fun hasPermission(permission: ClanPermission): Boolean {
        return role.hasPermission(permission)
    }

    fun toPlayer(): Player? {
        return Bukkit.getPlayer(name)
    }
}
