package ru.snapix.clan.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import ru.snapix.clan.api.Invite
import ru.snapix.clan.api.InviteStatus

class ClanResponseInviteEvent(val invite: Invite, val status: InviteStatus) : Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}