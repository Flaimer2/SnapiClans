package ru.snapix.clan.api

/**
 * The ClanPermission class provides access to the properties of the permission.
 *
 * @author Flaimer
 * @since 0.0.1
 * @see ClanRole
 */
enum class ClanPermission(val value: String) {
    INVITE("invite"),
    KICK("kick"),
    DISBAND("disband"),
    SET_ROLE("set_role")
}
