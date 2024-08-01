package ru.snapix.clan.api

import ru.snapix.clan.settings.Settings

/**
 * The ClanRole class provides access to the properties of the role.
 *
 * @author Flaimer
 * @since 2.0.0
 */
data class ClanRole(val name: String, val displayName: String, val weight: Int, val permissions: Set<ClanPermission>) {
    fun hasPermission(permission: ClanPermission): Boolean {
        return permissions.contains(permission)
    }

    companion object {
        val DEFAULT: ClanRole
            get() = Settings.config.roles().defaultRole()
        val OWNER: ClanRole
            get() = Settings.config.roles().ownerRole()
        fun other() = Settings.config.roles().otherRoles()

        fun role(name: String): ClanRole {
            return clanRoles()[name] ?: DEFAULT
        }

        fun role(weight: Int): ClanRole?  {
            return clanRolesWithWeight()[weight]
        }

        private fun clanRoles(): Map<String, ClanRole> {
            val map = mutableMapOf(OWNER.name to OWNER, DEFAULT.name to DEFAULT)
            other().forEach { map[it.name] = it }

            return map
        }

        private fun clanRolesWithWeight(): Map<Int, ClanRole> {
            val map = mutableMapOf(OWNER.weight to OWNER, DEFAULT.weight to DEFAULT)
            other().forEach { map[it.weight] = it }

            return map
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ClanRole) return false
        return other.weight == weight
    }

    override fun hashCode(): Int {
        return weight.hashCode()
    }
}