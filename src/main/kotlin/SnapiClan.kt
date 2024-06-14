package ru.snapix.clan

import org.bukkit.plugin.java.JavaPlugin
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import ru.snapix.clan.database.ClanDatabase

class SnapiClan : JavaPlugin() {
    val pool = JedisPool("localhost", 6379)
    val jedis: Jedis = pool.resource

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        ClanDatabase.load()
    }

    override fun onDisable() {
        ClanDatabase.unload()
    }

    companion object {
        @JvmStatic
        lateinit var instance: SnapiClan
    }
}

val snapiClan = SnapiClan.instance