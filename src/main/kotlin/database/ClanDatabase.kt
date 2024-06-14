package ru.snapix.clan.database

import kotlinx.coroutines.flow.*
import org.intellij.lang.annotations.Language
import ru.snapix.clan.api.Clan
import ru.snapix.clan.api.ClanRole
import ru.snapix.clan.api.User
import ru.snapix.clan.settings.Settings
import ru.snapix.library.database.*

object ClanDatabase {
    private var database: Database

    init {
        database = initializeDatabase {
            val config = Settings.database
            val hostAndPort = config.host().split(":")

            host = hostAndPort[0]
            port = hostAndPort[1].toInt()
            database = config.database()
            username = config.username()
            password = config.password()
        }
    }

    fun load() {
        database.transaction {
            execute(CREATE_TABLE_CLANS)
            execute(CREATE_TABLE_USERS)
        }
    }

    fun unload() {
        database.close()
    }


    fun createClan(clan: Clan) {
        database.useAsync { execute(CREATE_CLAN, clan.name, clan.displayName, clan.owner) }
    }

    fun createUser(user: User) {
        database.useAsync { execute(CREATE_USER, user.name, user.clanName, user.role.name) }
    }

    fun removeClan(name: String) {
        database.useAsync { execute(REMOVE_CLAN, name) }
    }

    fun removeUser(username: String) {
        database.useAsync { execute(REMOVE_USER, username) }
    }

    fun getClan(name: String): Clan? {
        val row = database.async { firstRow(SELECT_CLAN_BY_NAME, name) }

        return Clan(name, row?.getString("display_name") ?: return null, row.getString("owner") ?: return null)
    }
    
    fun getUser(username: String): User? {
        val row = database.async { firstRow(SELECT_USER_BY_NAME, username) }

        return User(username, ClanRole.role(row?.getString("role") ?: return null), row.getString("clan_name") ?: return null)
    }

    fun updateClan(clan: Clan) {
        database.useAsync { execute(UPDATE_CLAN, clan.displayName, clan.owner, clan.name) }
    }

    fun updateUser(user: User) {
        database.useAsync { execute(UPDATE_USER, user.role, user.name) }
    }

    fun getClans(): List<Clan> {
        return database.async {
            select(SELECT_CLANS).map { Clan(it.getString("name") ?: return@map null, it.getString("display_name") ?: return@map null, it.getString("owner") ?: return@map null) }.filterNotNull().toList()
        }
    }

    fun getUsers(): List<User> {
        return database.async {
            select(SELECT_USERS).map { User(it.getString("username") ?: return@map null, ClanRole.role(it.getString("role") ?: return@map null), it.getString("clan_name") ?: return@map null) }.filterNotNull().toList()
        }
    }

    @Language("SQL") private val CREATE_TABLE_CLANS = """
        CREATE TABLE IF NOT EXISTS `clan_clans`
        (
            `id` INTEGER NOT NULL AUTO_INCREMENT,
            `name` VARCHAR(32) NOT NULL,
            `display_name` VARCHAR(32) NOT NULL,           
            `owner` VARCHAR(32) NOT NULL,
            UNIQUE(`name`, `owner`),
            PRIMARY KEY(`id`)
        )
    """.trimIndent()
    @Language("SQL") private val CREATE_TABLE_USERS = """
        CREATE TABLE IF NOT EXISTS `clan_users`
        (
            `clan_name` VARCHAR(32) NOT NULL,
            `username` VARCHAR(32) NOT NULL,
            `role` VARCHAR(32) NOT NULL,
            UNIQUE(`clan_name`, `username`),
            FOREIGN KEY (`clan_name`) REFERENCES `clan_clans` (`name`) ON DELETE CASCADE,
            PRIMARY KEY(`username`)
        )
    """.trimIndent()

    @Language("SQL") private const val CREATE_CLAN = "INSERT IGNORE INTO clan_clans(`name`, `display_name`, `owner`) VALUES (?, ?, ?)"
    @Language("SQL") private const val CREATE_USER = "INSERT IGNORE INTO clan_users(`clan_name`, `username`, `role`) VALUES (?, ?, ?)"

    @Language("SQL") private const val REMOVE_CLAN = "DELETE FROM clan_clans WHERE `name` = ?"
    @Language("SQL") private const val REMOVE_USER = "DELETE FROM clan_users WHERE `username` = ?"

    @Language("SQL") private const val UPDATE_CLAN = "UPDATE clan_clans SET `display_name` = ?, `owner` = ? WHERE `name` = ?"
    @Language("SQL") private const val UPDATE_USER = "UPDATE clan_users SET `role` = ? WHERE `name` = ?"

    @Language("SQL") private const val SELECT_CLAN_BY_NAME = "SELECT * FROM clan_clans WHERE `name` = ?"
    @Language("SQL") private const val SELECT_USER_BY_NAME = "SELECT * FROM clan_users WHERE `username` = ?"

    @Language("SQL") private const val SELECT_CLANS = "SELECT * FROM clan_clans"
    @Language("SQL") private const val SELECT_USERS = "SELECT * FROM clan_users"
}