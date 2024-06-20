package ru.snapix.clan.messenger.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.snapix.clan.api.Clan
import ru.snapix.clan.api.ClanApi
import ru.snapix.clan.api.events.ChatMessageEvent
import ru.snapix.library.callEvent

@Serializable
@SerialName("chatmessage")
class ChatMessageAction(val sender: String, val receiver: String, val message: String) : Action() {
    override fun executeIncomingMessage() {
        val clan = ClanApi.clan(receiver) ?: return
        callEvent(ChatMessageEvent(sender, clan, message))
    }
}
