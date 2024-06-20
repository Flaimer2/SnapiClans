package ru.snapix.clan.caches

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.snapix.clan.api.Clan
import ru.snapix.clan.api.User
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.library.redis.async
import ru.snapix.library.redis.redisClient

object Clans {
    private const val KEY_REDIS_CLANS = "clan_clans"
    private const val KEY_REDIS_USERS = "clan_users"

    fun getClan(name: String): Clan? {
        return redisClient.async {
            val clan = hget(KEY_REDIS_CLANS, name.lowercase())

            if (clan != null) Json.decodeFromString<Clan>(clan) else updateClan(name)
        }
    }

    fun updateClan(name: String): Clan? {
        return redisClient.async {
            val clan = ClanDatabase.clan(name)

            if (clan != null) hset(KEY_REDIS_CLANS, name.lowercase() to Json.encodeToString(clan)) else hdel(
                KEY_REDIS_CLANS,
                name.lowercase()
            )

            clan
        }
    }

    fun updateClan(clan: Clan) {
        redisClient.async {
            hset(KEY_REDIS_CLANS, clan.name.lowercase() to Json.encodeToString(clan))
        }
    }

    fun getClans(): List<Clan> {
        return redisClient.async {
            hvals(KEY_REDIS_CLANS).map { Json.decodeFromString<Clan>(it) }
        }
    }

    fun getUser(name: String): User? {
        return redisClient.async {
            val user = hget(KEY_REDIS_USERS, name.lowercase())

            if (user != null) Json.decodeFromString<User>(user) else updateUser(name)
        }
    }

    fun updateUser(name: String): User? {
        return redisClient.async {
            val user = ClanDatabase.user(name)

            if (user != null) hset(KEY_REDIS_USERS, name.lowercase() to Json.encodeToString(user)) else hdel(
                KEY_REDIS_USERS,
                name.lowercase()
            )

            user
        }
    }

    fun updateUser(user: User) {
        redisClient.async {
            if (user.clan() != null) hset(
                KEY_REDIS_USERS,
                user.name.lowercase() to Json.encodeToString(user)
            ) else hdel(KEY_REDIS_USERS, user.name.lowercase())
        }
    }

    fun getUsers(): List<User> {
        return redisClient.async {
            hvals(KEY_REDIS_USERS).map { Json.decodeFromString<User>(it) }
        }
    }
}