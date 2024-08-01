package ru.snapix.clan

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import ru.snapix.clan.api.ClanApi
import ru.snapix.library.utils.toDate

class ClanExpansion : PlaceholderExpansion() {
    override fun getIdentifier() = "clan"
    override fun getAuthor() = "SnapiX"
    override fun getVersion() = "2.0.0"
    override fun persist() = true

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        val user = ClanApi.user(player.name)
        val clan = user?.clan()

        return when (params) {
            "name" -> clan?.name ?: "&cНет клана"
            "owner" -> clan?.owner ?: "&cНет владелец"
            "max_members" -> clan?.maxMembers?.toString() ?: "4"
            "members_size" -> clan?.users()?.size?.toString() ?: "0"
            "date_creation" -> clan?.formattedDateCreation() ?: ""
            "date_creation_without_hour" -> clan?.dateCreation?.toDate("dd/MM/yyyy") ?: ""
            "date_creation_raw" -> clan?.dateCreation?.toString() ?: ""
            "tag" -> clan?.tag ?: "&cНет"
            "role" -> user?.role?.displayName ?: "Участник"
            "role_raw" -> user?.role?.name ?: "default"
            "weight" -> user?.role?.weight?.toString() ?: "1"
            else -> null
        }
    }
}