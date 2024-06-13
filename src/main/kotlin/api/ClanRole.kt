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
        val DEFAULT: ClanRole = Settings.config.roles().defaultRole()
        val OWNER: ClanRole = Settings.config.roles().ownerRole()
        fun other() = Settings.config.roles().otherRoles()

        fun role(name: String): ClanRole {
            return clanRoles()[name] ?: DEFAULT
        }

        private fun clanRoles(): Map<String, ClanRole> {
            val map = mutableMapOf(OWNER.name to OWNER, DEFAULT.name to DEFAULT)
            other().forEach { map[it.name] = it }

            return map
        }
    }
}