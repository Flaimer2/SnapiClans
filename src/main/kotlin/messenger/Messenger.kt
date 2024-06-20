package ru.snapix.clan.messenger

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import ru.snapix.clan.KEY_REDIS_MESSENGER
import ru.snapix.clan.messenger.actions.Action
import ru.snapix.clan.messenger.actions.ChatMessageAction
import ru.snapix.clan.messenger.actions.ResponseInviteAction
import ru.snapix.clan.messenger.actions.SendInviteAction
import ru.snapix.clan.snapiClan
import ru.snapix.library.libs.kreds.connection.AbstractKredsSubscriber
import ru.snapix.library.redis.async
import ru.snapix.library.redis.redisClient
import ru.snapix.library.redis.subscribe

object Messenger {
    private val module = SerializersModule {
        polymorphic(Action::class) {
            subclass(ChatMessageAction::class)
            subclass(SendInviteAction::class)
            subclass(ResponseInviteAction::class)
        }
    }
    val json = Json { serializersModule = module }

    fun enable() {
        subscribe(object : AbstractKredsSubscriber() {
            val logger = snapiClan.logger

            override fun onMessage(channel: String, message: String) {
                val action = json.decodeFromString<Action>(message)
                action.executeIncomingMessage()
            }

            override fun onSubscribe(channel: String, subscribedChannels: Long) {
                logger.info("Success subscribed to channel: $channel")
            }

            override fun onUnsubscribe(channel: String, subscribedChannels: Long) {
                logger.info("Success unsubscribed to channel: $channel")
            }

            override fun onException(ex: Throwable) {
                logger.info("Exception while handling subscription to redis: ${ex.stackTrace}")
            }
        }, KEY_REDIS_MESSENGER)
    }

    fun sendOutgoingMessage(action: Action) {
        redisClient.async {
            publish(KEY_REDIS_MESSENGER, json.encodeToString(action))
        }
    }
}