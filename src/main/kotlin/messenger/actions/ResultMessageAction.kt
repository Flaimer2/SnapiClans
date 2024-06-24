package ru.snapix.clan.messenger.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import ru.snapix.clan.api.Clan
import ru.snapix.library.message

@Serializable
@SerialName("resultmessage")
class ResultMessageAction(val sender: String, val receiver: String, val clan: Clan, val message: String) : Action() {
    override fun executeIncomingMessage() {
        Bukkit.getPlayer(receiver)?.message(message, "sender" to sender, "receiver" to receiver, "clan" to clan.name)
    }
}
