package ru.snapix.clan.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import ru.snapix.clan.KEY_REDIS_INVITE
import ru.snapix.clan.caches.Clans
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.clan.messenger.Messenger
import ru.snapix.clan.messenger.actions.ChatMessageAction
import ru.snapix.clan.messenger.actions.ResponseInviteAction
import ru.snapix.clan.messenger.actions.ResultMessageAction
import ru.snapix.clan.settings.Settings
import ru.snapix.library.redis.async
import ru.snapix.library.redis.redisClient
import ru.snapix.library.redis.setOption
import kotlin.time.Duration.Companion.seconds

object ClanApi {
    fun createClan(name: String, owner: String) {
        val clan = Clan(name = name, owner = owner)

        ClanDatabase.createClan(clan)
        Clans.updateClan(name)

        createUser(name = owner, clanName = name, role = ClanRole.OWNER)
    }

    fun createUser(name: String, clanName: String, role: ClanRole = ClanRole.DEFAULT) {
        val user = User(name = name, clanName = clanName, role = role)

        ClanDatabase.createUser(user)
        Clans.updateUser(user)
    }

    fun removeClan(name: String) {
        val clan = Clans.getClan(name)

        ClanDatabase.removeClan(name)
        Clans.updateClan(name)

        clan?.users()?.forEach { Clans.updateUser(it) }
    }

    fun removeUser(name: String) {
        ClanDatabase.removeUser(name)
        Clans.updateUser(name)
    }

    fun updateClan(name: String, block: Clan.() -> Unit) {
        val clan = clan(name) ?: return

        clan.block()

        ClanDatabase.updateClan(clan)
        Clans.updateClan(name)
    }

    fun updateUser(name: String, block: User.() -> Unit) {
        val user = user(name) ?: return

        user.block()

        ClanDatabase.updateUser(user)
        Clans.updateUser(name)
    }

    fun clan(name: String): Clan? {
        return Clans.getClan(name)
    }

    fun user(name: String): User? {
        return Clans.getUser(name)
    }

    fun clans(): List<Clan> {
        return Clans.getClans()
    }

    fun users(): List<User> {
        return Clans.getUsers()
    }

    fun clans(block: (Clan) -> Boolean): List<Clan> {
        return clans().filter(block)
    }

    fun users(block: (User) -> Boolean): List<User> {
        return users().filter(block)
    }

    fun sendChatMessage(sender: Player, receiver: Clan, message: String) {
        val format = Settings.config.chatFormat()
        val msg = if (!sender.hasPermission("snapiclans.chat.color")) "&([A-z0-9])".toRegex()
            .replace(message, "") else ChatColor.translateAlternateColorCodes('&', message)
        val result = PlaceholderAPI.setPlaceholders(sender, format).replace("%message%", msg)
        Messenger.sendOutgoingMessage(ChatMessageAction(sender.name, receiver, result))
    }

    fun sendResultMessage(sender: String, receiver: String, clan: Clan, message: String) {
        Messenger.sendOutgoingMessage(ResultMessageAction(sender, receiver, clan, message))
    }

    fun sendInvite(clan: Clan, sender: String, receiver: String) {
        val invite = Invite(sender, receiver, clan)
        redisClient.async {
            sadd(KEY_REDIS_INVITE, Json.encodeToString(invite))
            CoroutineScope(Dispatchers.Default).launch {
                delay(Settings.config.inviteReplySeconds().seconds)

                if (!hasInvite(sender, receiver)) return@launch

                srem(KEY_REDIS_INVITE, Json.encodeToString(invite))

                val user = user(receiver)
                if (user == null || user.clan() != clan) {
                    Messenger.sendOutgoingMessage(
                        ResponseInviteAction(
                            invite,
                            InviteStatus.IGNORE
                        )
                    )
                }
            }
        }
        sendResultMessage(sender, receiver, clan, Settings.message.commands().invite().acceptOrDecline())
    }

    fun acceptInvite(invite: Invite) {
        redisClient.async {
            srem(KEY_REDIS_INVITE, Json.encodeToString(invite))
        }
        createUser(invite.receiver, invite.clan.name)
        Messenger.sendOutgoingMessage(ResponseInviteAction(invite, InviteStatus.ACCEPT))
    }

    fun declineInvite(invite: Invite) {
        redisClient.async {
            srem(KEY_REDIS_INVITE, Json.encodeToString(invite))
        }
        Messenger.sendOutgoingMessage(ResponseInviteAction(invite, InviteStatus.DECLINE))
    }

    fun getInvite(sender: String, receiver: String): Invite? {
        return redisClient.async {
            smembers(KEY_REDIS_INVITE).map { Json.decodeFromString<Invite>(it) }.find { it.sender.equals(sender, ignoreCase = true) && it.receiver.equals(receiver, ignoreCase = true) }
        }
    }

    fun getInviteByReceiver(receiver: String): List<Invite> {
        return redisClient.async {
            smembers(KEY_REDIS_INVITE).map { Json.decodeFromString<Invite>(it) }.filter { it.receiver.equals(receiver, ignoreCase = true) }
        }
    }

    fun getInviteBySender(sender: String): List<Invite> {
        return redisClient.async {
            smembers(KEY_REDIS_INVITE).map { Json.decodeFromString<Invite>(it) }.filter { it.sender.equals(sender, ignoreCase = true) }
        }
    }

    fun hasInvite(sender: String, receiver: String): Boolean {
        return getInvite(sender, receiver) != null
    }
}
