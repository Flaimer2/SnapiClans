package ru.snapix.clan.api

import ru.snapix.clan.settings.Settings

/**
 * The ClanRole class provides access to the properties of the role.
 *
 * @author Flaimer
 * @since 2.0.0
 */
//data class ClanRole(val name: String, val displayName: String, val weight: Int, val permissions: Set<ClanPermission>)
class ClanRole(val name: String, val displayName: String, val weight: Int, val permissions: Set<ClanPermission>) {
    fun hasPermission(permission: ClanPermission): Boolean {
        return permissions.contains(permission)
    }

    companion object {
        fun owner() = Settings.config.roles().ownerRole()
        fun default() = Settings.config.roles().defaultRole()
        fun other() = Settings.config.roles().otherRoles()

        fun role(name: String): ClanRole {
            return clanRoles()[name] ?: default()
        }

        private fun clanRoles(): Map<String, ClanRole> {
            val map = mutableMapOf<String, ClanRole>()
            owner().run {
                map[name] = this
            }
            default().run {
                map[name] = this
            }
            other().forEach { clanRoles[role.name] = role }
//            val clanRoles = mutableMapOf<String, ClanRole>()
//            owner().let {
//                clanRoles[it.name] = it
//            }
//            default().let {
//                clanRoles[it.name] = it
//            }
//            other().forEach { role -> clanRoles[role.name] = role }
//            return clanRoles
        }
    }
}