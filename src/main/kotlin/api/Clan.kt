package ru.snapix.clan.api

import kotlinx.serialization.Serializable

@Serializable
data class Clan(val name: String, var displayName: String, val owner: String) {
    fun getUsers(): List<User> {
        return getUsers { it.clanName == name }
    }
}
