package ru.snapix.clan.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.snapix.clan.api.ClanApi
import ru.snapix.clan.api.ClanPermission
import ru.snapix.clan.settings.Settings
import ru.snapix.library.*
import ru.snapix.library.libs.commands.BaseCommand
import ru.snapix.library.libs.commands.annotation.CatchUnknown
import ru.snapix.library.libs.commands.annotation.CommandAlias
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
    fun createClan(player: Player, args: Array<String>) {
        val config = commands.createClan()

        if (ClanApi.user(player.name) != null) {
            player.message(config.alreadyInClan())
            return
        }

        if (args.isEmpty() || args.size > 2) {
            player.message(config.use())
            return
        }

        val name = args[0]
        val displayName = if (args.size == 1) name else args[1]

        val regex = this.config.regex()
        if (!regex.clanName().toRegex().matches(name)) {
            player.message(config.clanNameInvalid())
            return
        }
        if (!regex.clanDisplayName().toRegex().matches(displayName)) {
            player.message(config.clanDisplayNameInvalid())
            return
        }

        if (ClanApi.clan(name) != null) {
            player.message(config.clanAlreadyCreate())
            return
        }

        player.withdrawMoney(
            amount = this.config.economy().createClan(),
            success = {
                ClanApi.createClan(name = name, displayName = displayName, owner = player.name)
                player.message(config.success())
            },
            fail = {
                player.message(config.noMoney())
            }
        )
    }

    @Subcommand("%clan_command_remove")
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
    fun chat(player: Player, args: Array<String>) {
        val config = commands.chat()
        val user = ClanApi.user(player.name)
        val clan = user?.clan()

        if (user == null || clan == null) {
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
    fun invite(player: Player, args: Array<String>) {
        val config = commands.invite()

        val sender = player.name
        val user = ClanApi.user(sender)
        val clan = user?.clan()

        if (user == null || clan == null) {
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

        val statusPlayer = StatusPlayer(receiver)

        if (!statusPlayer.isExist()) {
            player.message(config.notExist(), "name" to receiver)
            return
        }

        receiver = statusPlayer.name()

        if (!statusPlayer.isOnline()) {
            player.message(config.offline(), "name" to receiver)
            return
        }

        if (ClanApi.user(receiver) != null) {
            player.message(config.alreadyClan(), "name" to receiver)
            return
        }

        // TODO: Make check already invited

        ClanApi.sendInvite(clan, sender, receiver)
        player.message(config.success())
    }
}