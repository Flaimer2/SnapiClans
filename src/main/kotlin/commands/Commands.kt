package ru.snapix.clan.commands

import ru.snapix.clan.api.ClanApi
import ru.snapix.clan.settings.Settings
import ru.snapix.clan.snapiClan
import ru.snapix.library.addReplacements
import ru.snapix.library.libs.commands.PaperCommandManager
import ru.snapix.library.players


object Commands {
    private val manager = PaperCommandManager(snapiClan)

    fun enable() {
        registerCommandCompletions()
        registerCommandReplacements()
        manager.registerCommand(ClanCommand())
    }

    fun reload() {
        manager.unregisterCommands()
        manager.registerCommand(ClanCommand())
    }

    fun disable() {
        manager.unregisterCommands()
    }

    private fun registerCommandCompletions() {
        val commandCompletions = manager.commandCompletions
        commandCompletions.registerAsyncCompletion("playerwithoutclan") { context ->
            val players = players().toMutableList()
            players.removeAll(ClanApi.users().map { it.name })
            players.remove(context.player.name)
            players
        }
        commandCompletions.registerAsyncCompletion("playerwithoutclanforinvite") { context ->
            val players = players().toMutableList()
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

    private fun registerCommandReplacements() {
        val config = Settings.config.alias()

        manager.commandReplacements.addReplacements("clan_command_",
            "main" to config.mainCommand(),
            "help" to config.helpCommand(),
            "create" to config.createCommand(),
            "disband" to config.disbandCommand(),
            "invite" to config.inviteCommand(),
            "accept" to config.acceptCommand(),
            "decline" to config.declineCommand(),
            "role" to config.roleCommand(),
            "leave" to config.leaveCommand(),
            "chat" to config.chatCommand(),
            "remove" to config.removeCommand()
        )
    }
}