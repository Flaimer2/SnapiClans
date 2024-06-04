package ru.snapix.clan.api

data class Clan(private val id: Int, private val name: String, private var displayName: String, private val owner: String, private val members: List<User> = mutableListOf()) {
    fun addUser(member: User, role = ClanRole.) {

    }
}
