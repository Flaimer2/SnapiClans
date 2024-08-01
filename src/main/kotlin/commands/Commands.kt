package ru.snapix.clan.commands

import ru.snapix.clan.api.ClanApi
import ru.snapix.clan.plugin
import ru.snapix.clan.settings.Settings
import ru.snapix.library.SnapiLibrary
import ru.snapix.library.bukkit.BukkitCommands
import ru.snapix.library.bukkit.utils.addReplacements

object Commands : BukkitCommands(plugin, ClanCommand()) {
    override fun registerCommandCompletions() {
        val commandCompletions = manager.commandCompletions
        commandCompletions.registerAsyncCompletion("playerwithoutclan") { context ->
            val players = SnapiLibrary.getOnlinePlayers().map { it.getName() }.toMutableList()
            players.removeAll(ClanApi.users().map { it.name })
            players.remove(context.player.name)
            players
        }
        commandCompletions.registerAsyncCompletion("playerwithoutclanforinvite") { context ->
            val players = SnapiLibrary.getOnlinePlayers().map { it.getName() }.toMutableList()
            players.removeAll(ClanApi.users().map { it.name })
            if (players.contains(context.player.name)) emptyList<String>() else players
        }
        commandCompletions.registerAsyncCompletion("clan") { _ ->
            ClanApi.clans().map { it.name }
        }
        commandCompletions.registerAsyncCompletion("playerinmyclan") { context ->
            val user = ClanApi.user(context.player.name)
            val clan = user?.clan()
            clan?.users()?.map { it.name }?.filter { !it.equals(user.name, ignoreCase = true) } ?: emptyList<String>()
        }
        commandCompletions.registerAsyncCompletion("invitebyreceiver") { context ->
            ClanApi.getInviteByReceiver(context.player.name).map { it.sender }
        }
        commandCompletions.registerAsyncCompletion("nothing") { _ ->
            emptyList<String>()
        }
    }

    override fun registerCommandReplacements() {
        val config = Settings.config.alias()

        manager.commandReplacements.addReplacements("clan_command_",
            "main" to config.mainCommand(),
            "help" to config.helpCommand(),
            "create" to config.createCommand(),
            "disband" to config.disbandCommand(),
            "invite" to config.inviteCommand(),
            "accept" to config.acceptCommand(),
            "decline" to config.declineCommand(),
            "leave" to config.leaveCommand(),
            "chat" to config.chatCommand(),
            "remove" to config.removeCommand(),
            "info" to config.infoCommand(),
            "list" to config.listCommand(),
            "role_increase" to config.roleIncreaseCommand(),
            "role_decrease" to config.roleDecreaseCommand(),
            "tag" to config.tagCommand(),
        )
    }
}