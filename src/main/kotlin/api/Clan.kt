package ru.snapix.clan.api

import kotlinx.serialization.Serializable
import ru.snapix.clan.settings.Settings
import ru.snapix.library.utils.toDate

@Serializable
data class Clan(val name: String, var owner: String, var maxMembers: Int, var tag: String? = null, val dateCreation: Long) {
    fun users(): List<User> {
        return ClanApi.users { it.clanName == name }
    }

    fun formattedDateCreation(): String {
        return dateCreation.toDate(Settings.config.dateFormat())
    }
}