package ru.snapix.clan

import org.bukkit.plugin.java.JavaPlugin

class SnapiClan : JavaPlugin() {
    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {

    }

    companion object {
        lateinit var instance: SnapiClan
    }
}

val snapiClan = SnapiClan.instance