package ru.snapix.clan

import ru.snapix.clan.api.Clan
import ru.snapix.clan.api.Invite
import ru.snapix.clan.api.User

fun Clan?.placeholder(): Array<Pair<String, Any>> {
    return if (this != null) arrayOf(
        "clan" to name,
        "clan_owner" to owner,
        "clan_max_members" to maxMembers,
        "clan_date_creation" to formattedDateCreation(),
        "clan_members_size" to users().size,
        "clan_tag" to (tag ?: "")
    ) else emptyArray()
}

fun User?.placeholder(tag: String = "user"): Array<Pair<String, Any>> {
    return if (this != null) arrayOf(
        tag to name,
        "${tag}_role" to role.displayName,
        "${tag}_clan" to clanName
    ) else emptyArray()
}

fun Invite?.placeholder(): Array<Pair<String, Any>> {
    return if (this != null) arrayOf(
        "sender" to sender,
        "receiver" to receiver,
        *clan.placeholder()
    ) else emptyArray()
}

