package ru.snapix.clan.messenger.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import ru.snapix.clan.settings.Settings
import ru.snapix.library.message

@Serializable
@SerialName("sendinvite")
class SendInviteAction(val sender: String, val receiver: String, val clan: String) : Action() {
    override fun executeIncomingMessage() {
        val player = Bukkit.getPlayer(receiver) ?: return
        player.message(Settings.message.commands().invite().acceptOrDecline(), "sender" to sender, "receiver" to receiver, "clan_name" to clan)
    }
}
