package ru.snapix.clan.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import ru.snapix.clan.api.Clan
import ru.snapix.clan.api.ClanRole
import ru.snapix.clan.api.User
import ru.snapix.clan.settings.Settings

object ClanTable : Table("clan_clans") {
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", 32).uniqueIndex()
    val displayName: Column<String> = varchar("display_name", 32)
    val owner: Column<String> = varchar("owner", 32)

    override val primaryKey = PrimaryKey(id)
}

object UserTable : Table("clan_users") {
    val username = varchar("username", 32).uniqueIndex()
    val clanName = reference("clan_name", ClanTable.name, ReferenceOption.CASCADE).uniqueIndex()
    val role = varchar("role", 32)

    override val primaryKey = PrimaryKey(username)
}

object ClanDatabase {
    private var database: Database

    init {
        val config = Settings.database
        database = Database.connect(
            url = "jdbc:mariadb://${config.host()}/${config.database()}",
            driver = "org.mariadb.jdbc.Driver",
            user = config.username(),
            password = config.password()
        )
    }

    fun load() {
        transaction(database) {
            SchemaUtils.create(ClanTable, UserTable)
        }
    }

    fun createClan(clan: Clan) {
        transaction(database) {
            ClanTable.insert {
                it[name] = clan.name
                it[displayName] = clan.displayName
                it[owner] = clan.owner
            }
        }
    }

    fun createUser(user: User) {
        transaction(database) {
            UserTable.insert {
                it[username] = user.name
                it[clanName] = user.clanName
                it[role] = user.role.name
            }
        }
    }

    fun removeClan(name: String) {
        transaction(database) {
            ClanTable.deleteWhere { ClanTable.name eq name }
        }
    }

    fun removeUser(username: String) {
        transaction(database) {
            UserTable.deleteWhere { UserTable.username eq username }
        }
    }

    fun clan(name: String): Clan? {
        return transaction(database) {
            ClanTable.selectAll().where { ClanTable.name eq name }.map {
                Clan(it[ClanTable.name], it[ClanTable.displayName], it[ClanTable.owner])
            }
        }.firstOrNull()
    }

    fun user(username: String): User? {
        return transaction(database) {
            UserTable.selectAll().where { UserTable.username eq username }.map {
                User(it[UserTable.username], ClanRole.role(it[UserTable.role]), it[UserTable.clanName])
            }
        }.firstOrNull()
    }

    fun updateClan(clan: Clan) {
        transaction(database) {
            ClanTable.update({ ClanTable.name eq clan.name }) {
                it[displayName] = clan.displayName
                it[owner] = clan.owner
            }
        }
    }

    fun updateUser(user: User) {
        transaction(database) {
            UserTable.update({ UserTable.username eq user.name }) {
                it[role] = user.role.name
                it[clanName] = user.clanName
            }
        }
    }

    fun clans(): List<Clan> {
        return transaction(database) {
            ClanTable.selectAll().map {
                Clan(it[ClanTable.name], it[ClanTable.displayName], it[ClanTable.owner])
            }
        }
    }

    fun users(): List<User> {
        return transaction(database) {
            UserTable.selectAll().map {
                User(it[UserTable.username], ClanRole.role(it[UserTable.role]), it[UserTable.clanName])
            }
        }
    }
}