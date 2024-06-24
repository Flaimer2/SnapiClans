package ru.snapix.clan.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.snapix.clan.api.ClanApi
import ru.snapix.clan.api.ClanPermission
import ru.snapix.clan.api.ClanRole
import ru.snapix.clan.settings.Settings
import ru.snapix.clan.snapiClan
import ru.snapix.library.*
import ru.snapix.library.libs.commands.BaseCommand
import ru.snapix.library.libs.commands.annotation.CatchUnknown
import ru.snapix.library.libs.commands.annotation.CommandAlias
import ru.snapix.library.libs.commands.annotation.CommandCompletion
import ru.snapix.library.libs.commands.annotation.CommandPermission
import ru.snapix.library.libs.commands.annotation.Default
import ru.snapix.library.libs.commands.annotation.Subcommand

@CommandAlias("%clan_command_main")
class ClanCommand : BaseCommand() {
    private val config = Settings.config
    private val message = Settings.message
    private val commands = message.commands()

    @CatchUnknown
    @Default
    @Subcommand("%clan_command_help")
    fun help(sender: CommandSender) {
        sender.message(commands.help())
    }

    @Subcommand("%clan_command_create")
    @CommandCompletion("@nothing")
    fun createClan(player: Player, args: Array<String>) {
        val config = commands.createClan()

        if (ClanApi.user(player.name) != null) {
            player.message(config.alreadyInClan())
            return
        }

        if (args.isEmpty()) {
            player.message(config.use())
            return
        }
        val name = args[0]

        val regex = this.config.regex()
        if (!regex.clanName().toRegex().matches(name)) {
            player.message(config.clanNameInvalid())
            return
        }

        if (ClanApi.clan(name) != null) {
            player.message(config.clanAlreadyCreate())
            return
        }

        val networkPlayer = player.toNetworkPlayer()
        if (networkPlayer.getLevel() < this.config.level().createClan()) {
            player.message(config.levelLow())
            return
        }

        player.withdrawMoney(
            amount = this.config.economy().createClan(),
            success = {
                ClanApi.createClan(name = name, owner = player.name)
                player.message(config.success())
            },
            fail = {
                player.message(config.noMoney())
            }
        )
    }

    @Subcommand("%clan_command_disband")
    @CommandCompletion("@nothing")
    fun removeClan(player: Player, args: Array<String>) {
        val config = commands.removeClan()
        val user = ClanApi.user(player.name)

        if (user == null) {
            player.message(config.noClan())
            return
        }

        if (!user.hasPermission(ClanPermission.DISBAND)) {
            player.message(config.noPermission())
            return
        }

        if (args.size == 1 && args[0].lowercase() == "accept") {
            ClanApi.removeClan(user.clanName)
            player.message(config.success())
            return
        }

        player.message(config.accept())
    }

    @Subcommand("%clan_command_chat")
    @CommandCompletion("@nothing")
    fun chat(player: Player, args: Array<String>) {
        val config = commands.chat()

        val user = ClanApi.user(player.name)
        val clan = user?.clan()
        if (clan == null) {
            player.message(config.noClan())
            return
        }

        if (args.isEmpty()) {
            player.message(config.writeMessage())
            return
        }

        ClanApi.sendChatMessage(player, clan, args.joinToString(" "))
    }

    @Subcommand("%clan_command_invite")
    @CommandCompletion("@playerwithoutclanforinvite @nothing")
    fun invite(player: Player, args: Array<String>) {
        val config = commands.invite()
        val sender = player.name

        val user = ClanApi.user(sender)
        val clan = user?.clan()
        if (clan == null) {
            player.message(config.noClan())
            return
        }

        if (!user.hasPermission(ClanPermission.INVITE)) {
            player.message(config.noPermission())
            return
        }

        if (args.isEmpty()) {
            player.message(config.use())
            return
        }

        var receiver = args[0]
        if (receiver.equals(player.name, ignoreCase = true)) {
            player.message(config.cannotSelf())
            return
        }

        val networkReceiver = NetworkPlayer(receiver)

        if (!networkReceiver.isExist()) {
            player.message(config.notExist(), "name" to receiver)
            return
        }

        receiver = networkReceiver.name()

        if (!networkReceiver.isOnline()) {
            player.message(config.offline(), "name" to receiver)
            return
        }

        val level = networkReceiver.getLevel()
        if (level < this.config.level().joinClan()) {
            player.message(config.playerLevelLow(), "name" to receiver, "level" to level)
            return
        }

        if (ClanApi.user(receiver) != null) {
            player.message(config.alreadyClan(), "name" to receiver)
            return
        }

        if (ClanApi.hasInvite(sender, receiver)) {
            player.message(config.alreadyInvite(), "name" to receiver)
            return
        }

        ClanApi.sendInvite(clan, sender, receiver)
        player.message(config.success())
    }

    @Subcommand("%clan_command_accept")
    @CommandCompletion("@invitebyreceiver @nothing")
    fun accept(player: Player, args: Array<String>) {
        val config = commands.accept()
        val receiver = player.name

        val user = ClanApi.user(receiver)
        if (user != null) {
            player.message(config.alreadyClan())
            return
        }

        if (args.isEmpty()) {
            player.message(config.use())
            return
        }

        var sender = args[0]

        val invite = ClanApi.getInvite(sender, receiver)
        if (invite == null) {
            player.message(config.notInvite(), "name" to sender)
            return
        }

        sender = invite.sender

        val senderUser = ClanApi.user(sender)
        if (senderUser == null || senderUser.clan() != invite.clan) {
            player.message(config.errorNotInClan(), "name" to sender)
            return
        }

        if (!senderUser.hasPermission(ClanPermission.INVITE)) {
            player.message(config.errorDecrease(), "name" to sender)
            return
        }

        ClanApi.acceptInvite(invite)
        player.message(config.success(), "name" to sender, "clan" to invite.clan.name)
    }

    @Subcommand("%clan_command_decline")
    @CommandCompletion("@invitebyreceiver @nothing")
    fun decline(player: Player, args: Array<String>) {
        val config = commands.decline()
        val receiver = player.name

        if (args.isEmpty()) {
            player.message(config.use())
            return
        }
        var sender = args[0]

        val invite = ClanApi.getInvite(sender, receiver)
        if (invite == null) {
            player.message(config.notInvite(), "name" to sender)
            return
        }

        sender = invite.sender

        ClanApi.declineInvite(invite)
        player.message(config.success(), "name" to sender, "clan" to invite.clan.name)
    }

    @Subcommand("%clan_command_leave")
    @CommandCompletion("@nothing")
    fun leave(player: Player) {
        val config = commands.leave()

        val user = ClanApi.user(player.name)
        val clan = user?.clan()
        if (clan == null) {
            player.message(config.noClan())
            return
        }

        if (user.role == ClanRole.OWNER) {
            player.message(config.cantLeave())
            return
        }

        ClanApi.removeUser(user.name)
        player.message(config.success(), "clan" to clan.name)
    }

    @Subcommand("%clan_command_remove")
    @CommandCompletion("@playerinmyclan")
    fun remove(player: Player, args: Array<String>) {
        val config = commands.removeUser()

        val sender = player.name

        val user = ClanApi.user(sender)
        val clan = user?.clan()
        if (clan == null) {
            player.message(config.noClan())
            return
        }

        if (!user.hasPermission(ClanPermission.KICK)) {
            player.message(config.noPermission())
            return
        }

        if (args.isEmpty()) {
            player.message(config.use())
            return
        }
        val kicked = args[0]

        if (kicked.equals(sender, ignoreCase = true)) {
            player.message(config.cannotSelf())
            return
        }

        val kickedUser = ClanApi.user(kicked)
        val kickedClan = kickedUser?.clan()
        if (kickedClan == null) {
            player.message(config.playerNotClan())
            return
        }

        if (user.role.weight < kickedUser.role.weight) {
            player.message(config.playerBigger())
            return
        }

        if (user.role.weight == kickedUser.role.weight) {
            player.message(config.playerEquals())
            return
        }

        ClanApi.removeUser(kickedUser.name)
        ClanApi.sendResultMessage(sender, kickedUser.name, clan, config.successForKickedPlayer())
        player.message(config.success())
    }

    private val admin = commands.admin()

    @Subcommand("admin create")
    @CommandPermission("snapiclans.admin.create")
    @CommandCompletion("@nothing @playerwithoutclan")
    fun adminCreate(commandSender: CommandSender, args: Array<String>) {
        val config = admin.create()

        if (args.size < 2) {
            commandSender.message(config.use())
            return
        }
        val nameClan = args[0]
        val owner = args[1]

        if (ClanApi.user(owner)?.clan() != null) {
            commandSender.message(config.alreadyInClan())
            return
        }

        if (ClanApi.clan(nameClan) != null) {
            commandSender.message(config.clanAlreadyCreate())
            return
        }

        ClanApi.createClan(nameClan, owner)
        commandSender.message(config.success())
    }

    @Subcommand("admin disband")
    @CommandCompletion("@clan")
    @CommandPermission("snapiclans.admin.disband")
    fun adminRemoveClan(commandSender: CommandSender, args: Array<String>) {
        val config = admin.disband()

        if (args.isEmpty()) {
            commandSender.message(config.use())
            return
        }
        val nameClan = args[0]

        if (ClanApi.clan(nameClan) == null) {
            commandSender.message(config.clanNotCreate())
            return
        }

        ClanApi.removeClan(nameClan)
        commandSender.message(config.success())
    }

    @Subcommand("admin disband")
    @CommandCompletion("@nothing")
    @CommandPermission("snapiclans.admin.reload")
    fun reload(commandSender: CommandSender) {
        val config = admin.reload()

        snapiClan.reload()
        commandSender.message(config.success())
    }
}