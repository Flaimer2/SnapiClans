package ru.snapix.clan.settings

import ru.snapix.clan.api.ClanPermission
import ru.snapix.clan.api.ClanRole
import space.arim.dazzleconf.serialiser.Decomposer
import space.arim.dazzleconf.serialiser.FlexibleType
import space.arim.dazzleconf.serialiser.ValueSerialiser

class ClanRoleSerializer : ValueSerialiser<ClanRole> {
    override fun getTargetClass() = ClanRole::class.java

    override fun deserialise(flexibleType: FlexibleType): ClanRole {
        val map = flexibleType.map.mapKeys { it.key.string }
        val name = map[NAME]?.string ?: error("Can't determine name of clan role")
        val displayName = map[DISPLAY_NAME]?.string ?: error("Can't determine display_name of clan role")
        val weight = map[WEIGHT]?.integer ?: error("Can't determine weight of clan role")
        val permission = map[PERMISSIONS]?.getSet { it.getObject(ClanPermission::class.java) }
        val permissions: Set<ClanPermission> = if (permission?.map { it.value }?.contains("*") == true) ClanPermission.entries.toSet() else permission ?: emptySet()

        return ClanRole(name, displayName, weight, permissions)
    }

    override fun serialise(role: ClanRole, decomposer: Decomposer): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map[NAME] = role.name
        map[DISPLAY_NAME] = role.displayName
        map[WEIGHT] = role.weight
        map[PERMISSIONS] = if (role.permissions.containsAll(ClanPermission.entries)) listOf("*") else decomposer.decomposeCollection(ClanPermission::class.java, role.permissions)

        return map
    }

    companion object {
        private const val NAME = "name"
        private const val DISPLAY_NAME = "display_name"
        private const val WEIGHT = "weight"
        private const val PERMISSIONS = "permissions"
    }
}