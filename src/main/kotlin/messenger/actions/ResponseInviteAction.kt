package ru.snapix.clan.messenger.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import ru.snapix.clan.api.InviteStatus
import ru.snapix.clan.settings.Settings
import ru.snapix.library.message

@Serializable
@SerialName("responseinvite")
class ResponseInviteAction(val sender: String, val receiver: String, val clan: String, val status: InviteStatus) : Action() {
    override fun executeIncomingMessage() {
        println("$sender, $receiver, $clan, $status")
    }
}
