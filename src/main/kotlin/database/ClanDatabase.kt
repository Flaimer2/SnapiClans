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
    val owner: Column<String> = varchar("owner", 32)
    val maxMembers: Column<Int> = integer("max_members")
    val tag: Column<String?> = varchar("tag", 32).nullable().uniqueIndex()
    val dateCreation: Column<Long> = long("date_creation")

    override val primaryKey = PrimaryKey(id)
}

object UserTable : Table("clan_users") {
    val username = varchar("username", 32).uniqueIndex()
    val clanName = reference("clan_name", ClanTable.name, ReferenceOption.CASCADE)
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
                it[owner] = clan.owner
                it[maxMembers] = clan.maxMembers
                it[dateCreation] = clan.dateCreation
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
            ClanTable.selectAll().where { ClanTable.name eq name }.map(::toClan)
        }.firstOrNull()
    }

    fun user(username: String): User? {
        return transaction(database) {
            UserTable.selectAll().where { UserTable.username eq username }.map(::toUser)
        }.firstOrNull()
    }

    fun updateClan(clan: Clan) {
        transaction(database) {
            ClanTable.update({ ClanTable.name eq clan.name }) {
                it[owner] = clan.owner
                it[maxMembers] = clan.maxMembers
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
            ClanTable.selectAll().map(::toClan)
        }
    }

    fun users(): List<User> {
        return transaction(database) {
            UserTable.selectAll().map(::toUser)
        }
    }

    private fun toClan(row: ResultRow): Clan {
        return Clan(row[ClanTable.name], row[ClanTable.owner], row[ClanTable.maxMembers], row[ClanTable.tag], row[ClanTable.dateCreation])
    }

    private fun toUser(row: ResultRow): User {
        return User(row[UserTable.username], ClanRole.role(row[UserTable.role]), row[UserTable.clanName])
    }
}