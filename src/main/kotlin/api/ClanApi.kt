package ru.snapix.clan.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import ru.snapix.clan.KEY_REDIS_INVITE
import ru.snapix.clan.caches.Clans
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.clan.messenger.Messenger
import ru.snapix.clan.messenger.actions.ChatMessageAction
import ru.snapix.clan.messenger.actions.ResponseInviteAction
import ru.snapix.clan.messenger.actions.SendInviteAction
import ru.snapix.clan.settings.Settings
import ru.snapix.library.redis.async
import ru.snapix.library.redis.redisClient
import ru.snapix.library.redis.setOption

object ClanApi {
    fun createClan(name: String, displayName: String, owner: String) {
        val clan = Clan(name = name, displayName = displayName, owner = owner)

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

        Messenger.sendOutgoingMessage(ChatMessageAction(sender.name, receiver.name, result))
    }

    fun sendInvite(clan: Clan, sender: String, receiver: String) {
        redisClient.async {
            set(
                KEY_REDIS_INVITE,
                "$sender:$receiver:${clan.name}",
                setOption { exSeconds = Settings.config.inviteReplySeconds().toULong() })
            CoroutineScope(Dispatchers.Default).launch {
                delay(Settings.config.inviteReplySeconds() * 1000L)

                val user = user(receiver)
                if (user == null || user.clan() != clan) {
                    Messenger.sendOutgoingMessage(
                        ResponseInviteAction(
                            sender,
                            receiver,
                            clan.name,
                            InviteStatus.IGNORE
                        )
                    )
                }
            }
        }
        Messenger.sendOutgoingMessage(SendInviteAction(sender, receiver, clan.name))
    }

//    fun acceptInvite(clan: Clan, sender: String, receiver: String) {
//        Messenger.sendOutgoingMessage()
//    }
}
