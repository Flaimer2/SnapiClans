package ru.snapix.clan

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import ru.snapix.clan.api.ClanApi

class ClanExpansion : PlaceholderExpansion() {
    override fun getIdentifier() = "clan"
    override fun getAuthor() = "SnapiX"
    override fun getVersion() = "2.0.0"
    override fun persist() = true

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        val user = ClanApi.user(player.name)
        val clan = user?.clan() ?: return null

        return when (params) {
            "name" -> clan.name
            "owner" -> clan.owner
            "max_members" -> clan.maxMembers.toString()
            "date_creation" -> clan.formattedDateCreation()
            "date_creation_raw" -> clan.dateCreation.toString()
            "tag" -> clan.tag ?: return ""
            "role" -> user.role.displayName
            "role_raw" -> user.role.name
            "weight" -> user.role.weight.toString()
            else -> null
        }
    }
}