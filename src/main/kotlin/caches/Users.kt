package ru.snapix.clan.caches

import kotlinx.serialization.encodeToString
import ru.snapix.clan.api.User
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.library.cache.DatabaseCache
import ru.snapix.library.utils.json

object Users : DatabaseCache<User>() {
    override val KEY_REDIS = "clan-users"
    override fun key(value: User) = value.name
    override fun encode(value: User) = json.encodeToString(value)
    override fun decode(value: String) = json.decodeFromString<User>(value)
    override fun valueFromDatabase(key: String) = ClanDatabase.user(key)
}