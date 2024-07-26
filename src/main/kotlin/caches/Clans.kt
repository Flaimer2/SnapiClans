package ru.snapix.clan.caches

import kotlinx.serialization.encodeToString
import ru.snapix.clan.api.Clan
import ru.snapix.clan.database.ClanDatabase
import ru.snapix.library.cache.DatabaseCache
import ru.snapix.library.utils.json

object Clans : DatabaseCache<Clan>() {
    override val KEY_REDIS = "clan-clans"
    override fun key(value: Clan) = value.name
    override fun encode(value: Clan) = json.encodeToString(value)
    override fun decode(value: String) = json.decodeFromString<Clan>(value)
    override fun valueFromDatabase(key: String) = ClanDatabase.clan(key)
}