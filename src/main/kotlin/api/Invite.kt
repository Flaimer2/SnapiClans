package ru.snapix.clan.api

import kotlinx.serialization.Serializable

enum class InviteStatus {
    ACCEPT,
    DECLINE,
    IGNORE
}

@Serializable
data class Invite(val sender: String, val receiver: String, val clan: Clan)