package ru.snapix.clan.api

import ru.snapix.clan.caches.Clans
import ru.snapix.clan.database.ClanDatabase

fun createClan(name: String, displayName: String, owner: String) {
    val clan = Clan(name = name, displayName = displayName, owner = owner)

    ClanDatabase.createClan(clan)
    Clans.updateClan(name)

    createUser(name = owner, clanName = name, role = ClanRole.OWNER)
}

fun createUser(name: String, clanName: String, role: ClanRole = ClanRole.DEFAULT) {
    val user = User(name = name, clanName = clanName, role = role)

    ClanDatabase.createUser(user)
    Clans.updateUser(name)
}

fun removeClan(name: String) {
    ClanDatabase.removeClan(name)
    Clans.updateClan(name)
    Clans.getUsers()
}

fun removeUser(name: String) {
    ClanDatabase.removeUser(name)
    Clans.updateUser(name)
}

fun updateClan(name: String, block: Clan.() -> Unit) {
    val clan = getClan(name) ?: return

    clan.block()
    ClanDatabase.updateClan(clan)

    Clans.updateClan(name)
}

fun updateUser(name: String, block: User.() -> Unit) {
    val user = getUser(name) ?: return

    user.block()
    ClanDatabase.updateUser(user)

    Clans.updateUser(name)
}

fun getClan(name: String): Clan? {
    return Clans.getClan(name)
}

fun getUser(name: String): User? {
    return Clans.getUser(name)
}

fun getClans(): List<Clan> {
    return Clans.getClans()
}

fun getUsers(): List<User> {
    return Clans.getUsers()
}

fun getClans(block: (Clan) -> Boolean): List<Clan> {
    return getClans().filter(block)
}

fun getUsers(block: (User) -> Boolean): List<User> {
    return getUsers().filter(block)
}
