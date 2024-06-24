package ru.snapix.clan.messenger.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import ru.snapix.clan.SnapiClan
import ru.snapix.clan.api.ClanApi
import ru.snapix.clan.api.Invite
import ru.snapix.clan.api.InviteStatus
import ru.snapix.clan.settings.Settings
import ru.snapix.library.message

@Serializable
@SerialName("responseinvite")
class ResponseInviteAction(val invite: Invite, val status: InviteStatus) : Action() {
    override fun executeIncomingMessage() {
        val (sender, receiver) = invite
        val userSender = ClanApi.user(sender)
        val clanSender = userSender?.clan() ?: return

        when (status) {
            InviteStatus.ACCEPT -> userSender.toPlayer().message(Settings.message.responseInvite().accept(), "receiver" to receiver, "clan" to clanSender.name)
            InviteStatus.DECLINE -> userSender.toPlayer().message(Settings.message.responseInvite().decline(), "receiver" to receiver, "clan" to clanSender.name)
            InviteStatus.IGNORE -> userSender.toPlayer().message(Settings.message.responseInvite().ignore(), "receiver" to receiver, "clan" to clanSender.name)
        }
    }
}
