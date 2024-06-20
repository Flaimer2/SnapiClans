package ru.snapix.clan.commands

import ru.snapix.clan.settings.Settings
import ru.snapix.clan.snapiClan
import ru.snapix.library.addReplacements
import ru.snapix.library.libs.commands.PaperCommandManager

object Commands {
    private val manager = PaperCommandManager(snapiClan)

    fun enable() {
        registerCommandCompletions()
        registerCommandReplacements()
        manager.registerCommand(ClanCommand())
    }

    private fun registerCommandCompletions() {}

    private fun registerCommandReplacements() {
        val config = Settings.config.alias()

        manager.commandReplacements.addReplacements("clan_command_",
            "main" to config.mainCommand(),
            "help" to config.helpCommand(),
            "create" to config.createCommand(),
            "remove" to config.removeCommand(),
            "invite" to config.inviteCommand(),
            "accept" to config.acceptCommand(),
            "decline" to config.declineCommand(),
            "role" to config.roleCommand(),
            "leave" to config.leaveCommand(),
            "chat" to config.chatCommand()
        )
    }
}