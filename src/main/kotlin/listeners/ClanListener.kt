package ru.snapix.clan.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.snapix.clan.api.ClanApi
import ru.snapix.clan.api.InviteStatus
import ru.snapix.clan.api.events.ClanResponseInviteEvent
import ru.snapix.clan.placeholder
import ru.snapix.clan.settings.Settings
import ru.snapix.library.utils.message

class ClanListener : Listener {
    @EventHandler
    fun onClanResponseInvite(event: ClanResponseInviteEvent) {
        val invite = event.invite
        val sender = invite.sender
        val status = event.status

        val userSender = ClanApi.user(sender)
        userSender?.clan() ?: return

        val placeholder = event.invite.placeholder()
        when (status) {
            InviteStatus.ACCEPT -> userSender.toPlayer()?.message(Settings.message.responseInvite().accept(), *placeholder)
            InviteStatus.DECLINE -> userSender.toPlayer()?.message(Settings.message.responseInvite().decline(), *placeholder)
            InviteStatus.IGNORE -> userSender.toPlayer()?.message(Settings.message.responseInvite().ignore(), *placeholder)
        }
    }
}