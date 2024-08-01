package ru.snapix.clan.commands

import com.alonsoaliaga.alonsolevels.api.AlonsoLevelsAPI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.snapix.clan.PanelStorage
import ru.snapix.clan.api.*
import ru.snapix.clan.placeholder
import ru.snapix.clan.settings.Settings
import ru.snapix.library.SnapiLibrary
import ru.snapix.library.bukkit.SnapiLibraryBukkit
import ru.snapix.library.bukkit.utils.getMoney
import ru.snapix.library.bukkit.utils.withdrawMoney
import ru.snapix.library.libs.commands.BaseCommand
import ru.snapix.library.libs.commands.annotation.*
import ru.snapix.library.network.ServerType
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.utils.message
import ru.snapix.library.utils.translateAlternateColorCodes
import ru.snapix.profile.PanelStorage.otherProfile
import ru.snapix.profile.PanelStorage.profile

@CommandAlias("%clan_command_main")
class ClanCommand : BaseCommand() {
    private val config get() = Settings.config
    private val message get() = Settings.message
    private val commands get() = message.commands()

    @Default
    fun default(player: Player, args: Array<String>) {
        val config = Settings.message.commands().default()

        if (args.isEmpty()) {
            PanelStorage.clan(player)
            return
        }
        val name = args[0]

        val clan = ClanApi.clan(name)
        if (clan == null) {
            player.message(config.notFound())
            return
        }

        PanelStorage.otherClan(player, clan)
    }

    @CatchUnknown
    @Subcommand("%clan_command_help")
    @CommandCompletion("@nothing")
    fun help(sender: CommandSender) {
        sender.sendMessage(commands.help().map { translateAlternateColorCodes(it) }.toTypedArray())
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

        if (AlonsoLevelsAPI.getLevel(player.uniqueId) < this.config.level().createClan()) {
            player.message(config.levelLow())
            return
        }

        player.withdrawMoney(
            amount = this.config.economy().createClan(),
            success = { p ->
                ClanApi.createClan(name = name, owner = p.name)
                p.message(config.success(), *ClanApi.clan(name).placeholder(), *ClanApi.user(p.name).placeholder())
            },
            fail = { p ->
                p.message(config.noMoney(), "money" to p.getMoney())
            }
        )
    }

    @Subcommand("%clan_command_disband")
    @CommandCompletion("@nothing")
    fun removeClan(player: Player, args: Array<String>) {
        val config = commands.removeClan()

        val user = ClanApi.user(player.name)
        val clan = user?.clan()

        if (clan == null) {
            player.message(config.noClan())
            return
        }

        val placeholder = arrayOf(*clan.placeholder(), *user.placeholder())

        if (!user.hasPermission(ClanPermission.DISBAND)) {
            player.message(config.noPermission(), *placeholder)
            return
        }

        if (args.size == 1 && args[0].lowercase() == "accept") {
            ClanApi.removeClan(user.clanName)
            player.message(config.success(), *placeholder)
            return
        }

        player.message(config.accept(), *placeholder)
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

        val placeholder = arrayOf(*clan.placeholder(), *user.placeholder())

        if (!user.hasPermission(ClanPermission.INVITE)) {
            player.message(config.noPermission(), *placeholder)
            return
        }

        if (clan.users().size >= clan.maxMembers) {
            player.message(config.limitMaxMembers(), *placeholder)
            return
        }

        if (args.isEmpty()) {
            player.message(config.use(), *placeholder)
            return
        }

        var receiver = args[0]
        if (receiver.equals(player.name, ignoreCase = true)) {
            player.message(config.cannotSelf(), *placeholder)
            return
        }

        val networkReceiver = OfflineNetworkPlayer(receiver)

        if (!networkReceiver.hasPlayedBefore()) {
            player.message(config.notExist(), "receiver" to receiver, *placeholder)
            return
        }

        receiver = networkReceiver.getName()

        if (!networkReceiver.isOnline()) {
            player.message(config.offline(), "receiver" to receiver, *placeholder)
            return
        }

        if (ClanApi.user(receiver) != null) {
            player.message(config.alreadyClan(), "receiver" to receiver, *placeholder)
            return
        }

        if (ClanApi.hasInvite(sender, receiver)) {
            player.message(config.alreadyInvite(), "receiver" to receiver, *placeholder)
            return
        }

        val invites = clan.users().flatMap { ClanApi.getInviteBySender(it.name) }

        if (invites.size >= this.config.limitInviteForClan()) {
            player.message(config.limitInviteForClan())
            return
        }

        ClanApi.sendInvite(clan, sender, receiver)
        player.message(config.success(), "receiver" to receiver, *placeholder)
    }

    @Subcommand("%clan_command_accept")
    @CommandCompletion("@invitebyreceiver @nothing")
    fun accept(player: Player, args: Array<String>) {
        val config = commands.accept()
        val receiver = player.name

        val level = AlonsoLevelsAPI.getLevel(player.uniqueId)
        if (level < this.config.level().joinClan()) {
            player.message(commands.invite().playerLevelLow(), "receiver" to receiver, "level" to level)
            return
        }

        if (ClanApi.user(receiver) != null) {
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
            player.message(config.notInvite(), "sender" to sender)
            return
        }

        val placeholder = invite.placeholder()
        sender = invite.sender

        val senderUser = ClanApi.user(sender)
        val senderClan = senderUser?.clan()
        if (senderClan == null || senderClan.name != invite.clan.name) {
            player.message(config.errorNotInClan(), *placeholder)
            return
        }

        if (!senderUser.hasPermission(ClanPermission.INVITE)) {
            player.message(config.errorDecrease(), *placeholder)
            return
        }

        if (senderClan.users().size >= senderClan.maxMembers) {
            player.message(config.limitMaxMembers(), *placeholder)
            return
        }

        ClanApi.acceptInvite(invite)
        player.message(config.success(), *placeholder)
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
        val sender = args[0]

        val invite = ClanApi.getInvite(sender, receiver)
        if (invite == null) {
            player.message(config.notInvite(), "sender" to sender)
            return
        }

        val placeholder = invite.placeholder()

        ClanApi.declineInvite(invite)
        player.message(config.success(), *placeholder)
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

        val placeholder = arrayOf(*clan.placeholder(), *user.placeholder())

        if (user.role == ClanRole.OWNER) {
            player.message(config.cantLeave(), *placeholder)
            return
        }

        ClanApi.removeUser(user.name)
        player.message(config.success(), *placeholder)
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

        var placeholder = arrayOf(*clan.placeholder(), *user.placeholder())

        if (!user.hasPermission(ClanPermission.KICK)) {
            player.message(config.noPermission(), *placeholder)
            return
        }

        if (args.isEmpty()) {
            player.message(config.use(), *placeholder)
            return
        }
        val kicked = args[0]

        if (kicked.equals(sender, ignoreCase = true)) {
            player.message(config.cannotSelf(), *placeholder)
            return
        }

        val kickedUser = ClanApi.user(kicked)
        val kickedClan = kickedUser?.clan()
        if (kickedClan == null) {
            player.message(config.playerNotClan())
            return
        }

        placeholder = placeholder.plus(kickedUser.placeholder("kicked"))

        if (user.role.weight < kickedUser.role.weight) {
            player.message(config.playerBigger(), *placeholder)
            return
        }

        if (user.role.weight == kickedUser.role.weight) {
            player.message(config.playerEquals(), *placeholder)
            return
        }

        ClanApi.removeUser(kickedUser.name)
        OfflineNetworkPlayer(kickedUser.name).sendMessage(config.successForKickedPlayer(), *placeholder)
        player.message(config.success(), *placeholder)
    }

    @Subcommand("%clan_command_info")
    @CommandCompletion("@clan @nothing")
    fun info(player: Player, args: Array<String>) {
        val config = commands.info()

        if (args.isEmpty()) {
            val user = ClanApi.user(player.name)
            val clan = user?.clan()
            if (clan == null) {
                player.message(config.use())
                return
            }
            PanelStorage.userList(player)
        } else {
            val clanName = args[0]
            val clan = ClanApi.clan(clanName)
            if (clan == null) {
                player.message(config.notFoundClan())
                return
            }
            PanelStorage.otherClan(player, clan)
        }
    }

    @Subcommand("%clan_command_list")
    fun list(player: Player) {
        PanelStorage.clans(player)
    }

    @Subcommand("%clan_command_role_increase")
    @CommandCompletion("@playerinmyclan @nothing")
    fun roleIncrease(player: Player, args: Array<String>) {
        val config = commands.roleIncrease()

        val user = ClanApi.user(player.name)
        val clan = user?.clan()
        if (clan == null) {
            player.message(config.noClan())
            return
        }

        var placeholder = arrayOf(*clan.placeholder(), *user.placeholder())

        if (!user.hasPermission(ClanPermission.ROLE_INCREASE)) {
            player.message(config.noPermission(), *placeholder)
            return
        }

        if (args.isEmpty()) {
            player.message(config.use(), *placeholder)
            return
        }
        val increase = args[0]

        if (player.name.equals(increase, ignoreCase = true)) {
            player.message(config.cannotSelf(), *placeholder)
            return
        }

        val increaseUser = ClanApi.user(increase)
        if (increaseUser == null) {
            player.message(config.playerNotClan(), *placeholder)
            return
        }

        placeholder = placeholder.plus(increaseUser.placeholder("increase"))

        if (user.role.weight < increaseUser.role.weight) {
            player.message(config.playerBigger(), *placeholder)
            return
        }

        if (user.role.weight == increaseUser.role.weight) {
            player.message(config.playerEquals(), *placeholder)
            return
        }

        if (increaseUser.role.weight >= ClanRole.OWNER.weight - 1) {
            player.message(config.alreadyMax(), *placeholder)
            return
        }

        val nextRole = ClanRole.role(increaseUser.role.weight + 1)
        if (nextRole == null) {
            player.message(config.alreadyMax(), *placeholder)
            return
        }

        ClanApi.updateUser(increaseUser) {
            role = nextRole
        }
        player.message(config.success(), *placeholder, "clan_role" to nextRole.displayName)
        OfflineNetworkPlayer(increaseUser.name).sendMessage(config.successForPlayer(), *placeholder, "clan_role" to nextRole.displayName)
    }

    @Subcommand("%clan_command_role_decrease")
    @CommandCompletion("@playerinmyclan @nothing")
    fun roleDecrease(player: Player, args: Array<String>) {
        val config = commands.roleDecrease()

        val user = ClanApi.user(player.name)
        val clan = user?.clan()
        if (clan == null) {
            player.message(config.noClan())
            return
        }

        var placeholder = arrayOf(*clan.placeholder(), *user.placeholder())

        if (!user.hasPermission(ClanPermission.ROLE_DECREASE)) {
            player.message(config.noPermission(), *placeholder)
            return
        }

        if (args.isEmpty()) {
            player.message(config.use(), *placeholder)
            return
        }
        val decrease = args[0]

        if (player.name.equals(decrease, ignoreCase = true)) {
            player.message(config.cannotSelf(), *placeholder)
            return
        }

        val decreaseUser = ClanApi.user(decrease)
        if (decreaseUser == null) {
            player.message(config.playerNotClan(), *placeholder)
            return
        }

        placeholder = placeholder.plus(decreaseUser.placeholder("decrease"))

        if (user.role.weight < decreaseUser.role.weight) {
            player.message(config.playerBigger(), *placeholder)
            return
        }

        if (user.role.weight == decreaseUser.role.weight) {
            player.message(config.playerEquals(), *placeholder)
            return
        }

        if (ClanRole.DEFAULT.weight >= decreaseUser.role.weight) {
            player.message(config.alreadyMin(), *placeholder)
            return
        }

        val prevRole = ClanRole.role(decreaseUser.role.weight - 1)
        if (prevRole == null) {
            player.message(config.alreadyMin(), *placeholder)
            return
        }

        ClanApi.updateUser(decreaseUser) {
            role = prevRole
        }
        player.message(config.success(), *placeholder, "clan_role" to prevRole.displayName)
        OfflineNetworkPlayer(decreaseUser.name).sendMessage(config.successForPlayer(), *placeholder, "clan_role" to prevRole.displayName)
    }

    @Subcommand("%clan_command_tag")
    @CommandCompletion("@nothing")
    fun tag(player: Player, args: Array<String>) {
        val config = commands.tag()

        val user = ClanApi.user(player.name)
        var clan = user?.clan()
        if (user == null || clan == null) {
            player.message(config.noClan())
            return
        }

        val placeholder = arrayOf(*clan.placeholder(), *user.placeholder())

        if (!user.hasPermission(ClanPermission.SET_TAG)) {
            player.message(config.noPermission(), *placeholder)
            return
        }

        if (args.isEmpty()) {
            player.message(config.use(), *placeholder)
            return
        }
        val tag = args[0]

        val regex = this.config.regex()
        if (!regex.tag().toRegex().matches(tag)) {
            player.message(config.tagInvalid())
            return
        }

        if (ClanApi.clans { it.tag.equals(tag, true) }.isNotEmpty()) {
            player.message(config.tagUsed(), *placeholder)
            return
        }

        player.withdrawMoney(
            amount = this.config.economy().tag(),
            success = { p ->
                clan = ClanApi.updateClan(clan!!) {
                    this.tag = tag
                }
                p.message(config.success(), *clan.placeholder(), *user.placeholder())
            },
            fail = { p ->
                p.message(config.noMoney(), *clan.placeholder(), *user.placeholder(), "money" to p.getMoney())
            }
        )
    }

    private val admin
        get() = commands.admin()

    @Subcommand("admin create")
    @CommandPermission("snapiclans.admin.create")
    @CommandCompletion("@nothing @playerwithoutclan")
    fun adminCreate(commandSender: CommandSender, args: Array<String>) {
        val config = admin.create()

        if (args.size < 2) {
            commandSender.sendMessage(translateAlternateColorCodes(config.use()))
            return
        }
        val clanName = args[0]
        val owner = args[1]

        if (ClanApi.user(owner)?.clan() != null) {
            commandSender.sendMessage(translateAlternateColorCodes(config.alreadyInClan()))
            return
        }

        if (ClanApi.clan(clanName) != null) {
            commandSender.sendMessage(translateAlternateColorCodes(config.clanAlreadyCreate()))
            return
        }

        ClanApi.createClan(clanName, owner)
        commandSender.sendMessage(translateAlternateColorCodes(config.success()))
    }

    @Subcommand("admin disband")
    @CommandCompletion("@clan")
    @CommandPermission("snapiclans.admin.disband")
    fun adminRemoveClan(commandSender: CommandSender, args: Array<String>) {
        val config = admin.disband()

        if (args.isEmpty()) {
            commandSender.sendMessage(translateAlternateColorCodes(config.use()))
            return
        }
        val clanName = args[0]

        if (ClanApi.clan(clanName) == null) {
            commandSender.sendMessage(translateAlternateColorCodes(config.clanNotCreate()))
            return
        }

        ClanApi.removeClan(clanName)
        commandSender.sendMessage(translateAlternateColorCodes(config.success()))
    }

    @Subcommand("admin createuser")
    @CommandPermission("snapiclans.admin.createuser")
    @CommandCompletion("@nothing")
    fun adminCreateUser(commandSender: CommandSender, args: Array<String>) {
        val config = admin.createUser()

        if (args.size < 3) {
            commandSender.sendMessage(translateAlternateColorCodes(config.use()))
            return
        }
        val clanName = args[0]
        val username = args[1]
        val role = ClanRole.role(args[2])

        if (ClanApi.clan(clanName) == null) {
            commandSender.sendMessage(translateAlternateColorCodes(config.clanNotCreate()))
            return
        }

        if (ClanApi.user(username) != null) {
            commandSender.sendMessage(translateAlternateColorCodes(config.userAlreadyInClan()))
            return
        }

        ClanApi.createUser(username, clanName, role)
        commandSender.sendMessage(translateAlternateColorCodes(config.success()))
    }

    @Subcommand("admin reload")
    @CommandCompletion("@nothing")
    @CommandPermission("snapiclans.admin.reload")
    fun reload(commandSender: CommandSender) {
        val config = admin.reload()

        Settings.reload()
        commandSender.sendMessage(translateAlternateColorCodes(config.success()))
    }
}