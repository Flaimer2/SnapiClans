package ru.snapix.clan.caches

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.snapix.clan.api.Clan
import ru.snapix.clan.api.User
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.clan.snapiClan
import ru.snapix.library.async

object Clans {
    private const val KEY_REDIS_CLANS = "clan_clans"
    private const val KEY_REDIS_USERS = "clan_users"

    fun getClan(name: String): Clan? {
        return snapiClan.jedis.async {
            val clan = hget(KEY_REDIS_CLANS, name.lowercase())

            if (clan != null) Json.decodeFromString<Clan>(clan) else updateClan(name)
        }
    }

    fun updateClan(name: String): Clan? {
        return snapiClan.jedis.async {
            val clan = ClanDatabase.getClan(name)

            if (clan != null) hset(KEY_REDIS_CLANS, mapOf(name.lowercase() to Json.encodeToString(clan))) else hdel(KEY_REDIS_CLANS, name.lowercase())

            clan
        }
    }

    fun getClans(): List<Clan> {
        TODO()
    }

    fun getUser(name: String): User? {
        return snapiClan.jedis.async {
            val user = hget(KEY_REDIS_USERS, name.lowercase())

            if (user != null) Json.decodeFromString<User>(user) else updateUser(name)
        }
    }

    fun updateUser(name: String): User? {
        return snapiClan.jedis.async {
            val user = ClanDatabase.getUser(name)

            if (user != null) hset(KEY_REDIS_USERS, mapOf(name.lowercase() to Json.encodeToString(user))) else hdel(KEY_REDIS_USERS, name.lowercase())

            user
        }
    }

    fun getUsers(): List<User> {
        TODO()
    }
}