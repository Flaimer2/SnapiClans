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
    ROLE_INCREASE("role_increase"),
    ROLE_DECREASE("role_decrease"),
    SET_TAG("set_tag")
}