package ru.snapix.clan.api

import kotlinx.serialization.Serializable
import ru.snapix.clan.ClanRoleSerializer

@Serializable
data class User(val name: String, @Serializable(with = ClanRoleSerializer::class) var role: ClanRole, val clanName: String) {
    fun clan(): Clan? = getClan(clanName)

    fun hasPermission(permission: ClanPermission): Boolean {
        return role.hasPermission(permission)
    }
}
