package ru.snapix.clan.api

import kotlinx.serialization.Serializable

@Serializable
data class Clan(val name: String, var owner: String) {
    fun users(): List<User> {
        return ClanApi.users { it.clanName == name }
    }
}
