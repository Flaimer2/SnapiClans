package ru.snapix.clan.settings

import ru.snapix.clan.api.ClanPermission
import ru.snapix.clan.api.ClanRole
import ru.snapix.library.libs.dazzleconf.annote.ConfDefault.*
import ru.snapix.library.libs.dazzleconf.annote.ConfKey
import ru.snapix.library.libs.dazzleconf.annote.SubSection

interface MainConfig {
    @SubSection
    fun alias(): Alias
    interface Alias {
        @ConfKey("main-command")
        @DefaultString("clan|clans|guilds|guild")
        fun mainCommand(): String

        @ConfKey("help-command")
        @DefaultString("help")
        fun helpCommand(): String

        @ConfKey("create-command")
        @DefaultString("create")
        fun createCommand(): String

        @ConfKey("disband-command")
        @DefaultString("disband")
        fun disbandCommand(): String

        @ConfKey("invite-command")
        @DefaultString("invite")
        fun inviteCommand(): String

        @ConfKey("accept-command")
        @DefaultString("accept")
        fun acceptCommand(): String

        @ConfKey("decline-command")
        @DefaultString("decline")
        fun declineCommand(): String

        @ConfKey("leave-command")
        @DefaultString("leave")
        fun leaveCommand(): String

        @ConfKey("remove-command")
        @DefaultString("remove")
        fun removeCommand(): String

        @ConfKey("chat-command")
        @DefaultString("chat")
        fun chatCommand(): String

        @ConfKey("members-command")
        @DefaultString("members")
        fun membersCommand(): String

        @ConfKey("info-command")
        @DefaultString("info")
        fun infoCommand(): String

        @ConfKey("role-increase-command")
        @DefaultString("role increase")
        fun roleIncreaseCommand(): String

        @ConfKey("role-decrease-command")
        @DefaultString("role decrease")
        fun roleDecreaseCommand(): String

        @ConfKey("tag-command")
        @DefaultString("tag")
        fun tagCommand(): String
    }

    @SubSection
    fun roles(): Roles
    interface Roles {
        @ConfKey("default-role")
        @DefaultObject("defaultRoleDefault")
        fun defaultRole(): ClanRole

        @ConfKey("owner-role")
        @DefaultObject("ownerRoleDefault")
        fun ownerRole(): ClanRole

        @ConfKey("other-roles")
        @DefaultObject("otherRolesDefault")
        fun otherRoles(): List<ClanRole>

        companion object {
            @JvmStatic
            @Suppress("unused")
            fun defaultRoleDefault() = ClanRole("default", "Участник", 1, emptySet())

            @JvmStatic
            @Suppress("unused")
            fun ownerRoleDefault() = ClanRole("owner", "Владелец", 4, ClanPermission.entries.toSet())

            @JvmStatic
            @Suppress("unused")
            fun otherRolesDefault() = listOf(
                ClanRole("veteran", "Ветеран", 2, setOf(ClanPermission.INVITE, ClanPermission.KICK)),
                ClanRole(
                    "vice",
                    "Заместитель",
                    3,
                    setOf(ClanPermission.INVITE, ClanPermission.KICK, ClanPermission.ROLE_INCREASE, ClanPermission.ROLE_DECREASE)
                )
            )
        }
    }

    @SubSection
    fun regex(): Regex
    interface Regex {
        @ConfKey("clan-name")
        @DefaultString("[A-z0-9]{3,16}")
        fun clanName(): String
    }

    @SubSection
    fun economy(): Economy
    interface Economy {
        @ConfKey("create-clan")
        @DefaultInteger(1000)
        fun createClan(): Int

        @ConfKey("tag")
        @DefaultInteger(1000)
        fun tag(): Int
    }

    @SubSection
    fun level(): Level
    interface Level {
        @ConfKey("create-clan")
        @DefaultInteger(7)
        fun createClan(): Int

        @ConfKey("join-clan")
        @DefaultInteger(7)
        fun joinClan(): Int
    }

    @ConfKey("chat-format")
    @DefaultString("%player_name% > %message%")
    fun chatFormat(): String

    @ConfKey("date-format")
    @DefaultString("dd/MM/yyyy hh:mma")
    fun dateFormat(): String

    @ConfKey("max-members")
    @DefaultInteger(10)
    fun maxMembers(): Int

    @ConfKey("invite-reply-seconds")
    @DefaultInteger(20)
    fun inviteReplySeconds(): Int

    @ConfKey("limit-invite-for-clan")
    @DefaultInteger(5)
    fun limitInviteForClan(): Int
}