package ru.snapix.clan.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import ru.snapix.clan.api.Clan

class ChatMessageEvent(val sender: String, val receiver: Clan, val message: String) : Event() {
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