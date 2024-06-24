package ru.snapix.clan.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ru.snapix.clan.api.events.ChatMessageEvent

class ChatListener : Listener {
    @EventHandler
    fun onChat(event: ChatMessageEvent) {
        for (user in event.receiver.users()) {
            user.toPlayer()?.sendMessage(event.message)
        }
    }
}