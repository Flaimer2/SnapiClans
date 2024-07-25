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
import ru.snapix.clan.api.events.ClanResponseInviteEvent
import ru.snapix.clan.caches.Clans
import ru.snapix.clan.caches.Users
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.clan.settings.Settings
import ru.snapix.library.bukkit.utils.callEvent
import ru.snapix.library.bukkit.utils.sendMessage
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.network.player.OnlineNetworkPlayer
import ru.snapix.library.utils.async
import ru.snapix.library.utils.redisClient
import ru.snapix.library.utils.stripColor
import ru.snapix.library.utils.translateAlternateColorCodes
import kotlin.time.Duration.Companion.seconds

object ClanApi {
    fun createClan(name: String, owner: String) {
        val clan = Clan(name = name, owner = owner, maxMembers = Settings.config.maxMembers(), dateCreation = System.currentTimeMillis())

        ClanDatabase.createClan(clan)
        Clans.update(clan)

        createUser(name = owner, clanName = name, role = ClanRole.OWNER)
    }

    fun createUser(name: String, clanName: String, role: ClanRole = ClanRole.DEFAULT) {
        val user = User(name = name, clanName = clanName, role = role)

        ClanDatabase.createUser(user)
        Users.update(user)
    }

    fun removeClan(name: String) {
        val clan = Clans[name]

        ClanDatabase.removeClan(name)
        Clans.update(name)

        clan?.users()?.forEach { Users.update(it.name) }
    }

    fun removeUser(name: String) {
        ClanDatabase.removeUser(name)
        Users.update(name)
    }

    fun updateClan(clan: Clan, block: Clan.() -> Unit): Clan {
        clan.block()

        ClanDatabase.updateClan(clan)
        Clans.update(clan)

        return clan
    }

    fun updateClan(name: String, block: Clan.() -> Unit): Clan? {
        val clan = clan(name) ?: return null
        return updateClan(clan, block)
    }

    fun updateUser(user: User, block: User.() -> Unit): User {
        user.block()

        ClanDatabase.updateUser(user)
        Users.update(user)

        return user
    }

    fun updateUser(name: String, block: User.() -> Unit): User? {
        val user = user(name) ?: return null
        return updateUser(user, block)
    }

    fun clan(name: String): Clan? {
        return Clans[name]
    }

    fun user(name: String): User? {
        return Users[name]
    }

    fun clans(): List<Clan> {
        return Clans.values()
    }

    fun users(): List<User> {
        return Users.values()
    }

    fun clans(block: (Clan) -> Boolean): List<Clan> {
        return clans().filter(block)
    }

    fun users(block: (User) -> Boolean): List<User> {
        return users().filter(block)
    }

    fun sendChatMessage(sender: Player, receiver: Clan, message: String) {
        val format = Settings.config.chatFormat()

        val msg = if (!sender.hasPermission("snapiclans.chat.color")) stripColor(message) else translateAlternateColorCodes('&', message)
        val result = translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender, format)).replace("%message%", msg)

        val networkPlayers = listOf(OnlineNetworkPlayer(sender.name), *receiver.users().map { OfflineNetworkPlayer(it.name) }.toTypedArray())
        networkPlayers.sendMessage(result)
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
                    callEvent(ClanResponseInviteEvent(invite, InviteStatus.IGNORE))
                }
            }
        }
        OfflineNetworkPlayer(receiver).sendMessage(Settings.message.commands().invite().acceptOrDecline(), "name" to sender, "clan" to clan)
    }

    fun acceptInvite(invite: Invite) {
        redisClient.async {
            srem(KEY_REDIS_INVITE, Json.encodeToString(invite))
        }
        createUser(invite.receiver, invite.clan.name)
        callEvent(ClanResponseInviteEvent(invite, InviteStatus.ACCEPT))
    }

    fun declineInvite(invite: Invite) {
        redisClient.async {
            srem(KEY_REDIS_INVITE, Json.encodeToString(invite))
        }
        callEvent(ClanResponseInviteEvent(invite, InviteStatus.DECLINE))
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
