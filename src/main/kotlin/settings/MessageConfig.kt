package ru.snapix.clan.settings

import ru.snapix.library.libs.dazzleconf.annote.ConfDefault.DefaultString
import ru.snapix.library.libs.dazzleconf.annote.ConfDefault.DefaultStrings
import ru.snapix.library.libs.dazzleconf.annote.ConfKey
import ru.snapix.library.libs.dazzleconf.annote.SubSection

interface MessageConfig {
    @ConfKey("response-invite")
    @SubSection
    fun responseInvite(): ResponseInvite
    interface ResponseInvite {
        @ConfKey("accept")
        @DefaultString("Игрок %receiver% принял ваше приглашение в клан %clan%")
        fun accept(): String

        @ConfKey("decline")
        @DefaultString("Игрок %receiver% отказался от вашего приглашения в клан %clan%")
        fun decline(): String

        @ConfKey("ignore")
        @DefaultString("Игрок %receiver% проигнорировал ваше приглашение в клан %clan%")
        fun ignore(): String
    }

    @SubSection
    fun commands(): Commands
    interface Commands {
        @DefaultStrings("Hello", "world??", "!")
        fun help(): List<String>

        @ConfKey("create-clan")
        @SubSection
        fun createClan(): CreateCommand
        interface CreateCommand {
            @ConfKey("already-in-clan")
            @DefaultString("&cВы уже в клане")
            fun alreadyInClan(): String

            @ConfKey("use")
            @DefaultString("&cИспользуйте: &a/clans create <название> [отображаемое название]")
            fun use(): String

            @ConfKey("clan-name-invalid")
            @DefaultString("&cВы не правильно написали название клана")
            fun clanNameInvalid(): String

            @ConfKey("clan-displayname-invalid")
            @DefaultString("&cВы не правильно написали отображаемое название клана")
            fun clanDisplayNameInvalid(): String

            @ConfKey("clan-already-create")
            @DefaultString("&cКлан c таким именем уже создан")
            fun clanAlreadyCreate(): String

            @ConfKey("level-low")
            @DefaultString("У вас низкий уровень. Нужен 7")
            fun levelLow(): String

            @ConfKey("no-money")
            @DefaultString("&cУ вас денег нет")
            fun noMoney(): String

            @ConfKey("success")
            @DefaultString("&aВы успешно создали клан")
            fun success(): String
        }

        @ConfKey("remove-clan")
        @SubSection
        fun removeClan(): RemoveCommand
        interface RemoveCommand {
            @ConfKey("no-clan")
            @DefaultString("&cВы не в клане")
            fun noClan(): String

            @ConfKey("no-permission")
            @DefaultString("&cУ вас нет разрешения, чтобы сделать это")
            fun noPermission(): String

            @ConfKey("accept")
            @DefaultString("&aНажмите, чтобы потвердить удаление")
            fun accept(): String

            @ConfKey("success")
            @DefaultString("&aВы успешно удалили клан")
            fun success(): String
        }

        @SubSection
        fun invite(): InviteCommand
        interface InviteCommand {
            @ConfKey("no-clan")
            @DefaultString("&cВы не в клане")
            fun noClan(): String

            @ConfKey("no-permission")
            @DefaultString("&cУ вас нет разрешения, чтобы отправлять приглашение")
            fun noPermission(): String

            @ConfKey("use")
            @DefaultString("&fИспользуйте: /clans invite player")
            fun use(): String

            @ConfKey("cannot-self")
            @DefaultString("&fВы не можете пригласить себя")
            fun cannotSelf(): String

            @ConfKey("not-exist-receiver")
            @DefaultString("&fВы не можете пригласить игрока %name%, так как его не существует")
            fun notExist(): String

            @ConfKey("offline-receiver")
            @DefaultString("&fВы не можете пригласить игрока %name%, так как он не в сети")
            fun offline(): String

            @ConfKey("already-in-clan")
            @DefaultString("&cИгрок %name% уже в клане")
            fun alreadyClan(): String

            @ConfKey("already-invite")
            @DefaultString("&cВы уже пригласили игрока %name%")
            fun alreadyInvite(): String

            @ConfKey("player-level-low")
            @DefaultString("У игрока низкий уровень. Ему нужен 7")
            fun playerLevelLow(): String

            @ConfKey("accept-or-decline")
            @DefaultString("&aНажмите, чтобы ответить на приглашение (clans accept player, clans decline player)")
            fun acceptOrDecline(): String

            @ConfKey("success")
            @DefaultString("&aВы отправили приглашение")
            fun success(): String
        }

        @SubSection
        fun accept(): AcceptCommand
        interface AcceptCommand {
            @ConfKey("already-in-clan")
            @DefaultString("&cВы уже в клане")
            fun alreadyClan(): String

            @ConfKey("use")
            @DefaultString("&fИспользуйте: /clans accept player")
            fun use(): String

            @ConfKey("player-not-invite")
            @DefaultString("&aИгрок %name% не приглашал вас в клан")
            fun notInvite(): String

            @ConfKey("error-sender-not-in-clan")
            @DefaultString("&aИгрок, который вас пригласил, вышел из клана, поэтому вы не можете принять его приглашение")
            fun errorNotInClan(): String

            @ConfKey("error-sender-decrease")
            @DefaultString("&aИгрок, который вас пригласил, был понижен, поэтому вы не можете принять его приглашение")
            fun errorDecrease(): String

            @ConfKey("success")
            @DefaultString("&aВы вступили в клан %clan%")
            fun success(): String
        }

        @SubSection
        fun decline(): DeclineCommand
        interface DeclineCommand {
            @ConfKey("use")
            @DefaultString("&fИспользуйте: /clans decline player")
            fun use(): String

            @ConfKey("player-not-invite")
            @DefaultString("&aИгрок %name% не приглашал вас в клан")
            fun notInvite(): String

            @ConfKey("success")
            @DefaultString("&aВы вступили в клан %clan%")
            fun success(): String
        }

        @SubSection
        fun chat(): ChatCommand
        interface ChatCommand {
            @ConfKey("no-clan")
            @DefaultString("&cВы не в клане")
            fun noClan(): String

            @ConfKey("write-message")
            @DefaultString("&aНапишите сообщение")
            fun writeMessage(): String
        }

        @SubSection
        fun role(): RoleCommand
        interface RoleCommand {
            @ConfKey("no-clan")
            @DefaultString("&cВы не в клане")
            fun noClan(): String

            @ConfKey("no-permission")
            @DefaultString("&cУ вас нет разрешения, чтобы изменить роль")
            fun noPermission(): String

            @ConfKey("use")
            @DefaultString("&cИспользуйте: /clan role increase/decrease <имя>")
            fun use(): String

            @ConfKey("success-increase")
            @DefaultString("&cВы успешно повысили %player_name%")
            fun successIncrease(): String

            @ConfKey("success-decrease")
            @DefaultString("&cВы успешно понизили %player_name%")
            fun successDecrease(): String
        }

        @SubSection
        fun leave(): LeaveCommand
        interface LeaveCommand {
            @ConfKey("no-clan")
            @DefaultString("&cВы не в клане")
            fun noClan(): String

            @ConfKey("you-cant-leave-clan")
            @DefaultString("&cВы не можете выйти из клана, так как вы его создатель")
            fun cantLeave(): String

            @ConfKey("success")
            @DefaultString("&cВы вышли из клана")
            fun success(): String
        }

        @SubSection
        fun removeUser(): RemoveUserCommand
        interface RemoveUserCommand {
            @ConfKey("no-clan")
            @DefaultString("&cВы не в клане")
            fun noClan(): String

            @ConfKey("no-permission")
            @DefaultString("&cУ вас нет разрешения, чтобы удалять игроков")
            fun noPermission(): String

            @ConfKey("use")
            @DefaultString("&fИспользуйте: /clan remove player")
            fun use(): String

            @ConfKey("cannot-self")
            @DefaultString("&fВы не можете удалить себя")
            fun cannotSelf(): String

            @ConfKey("player-not-clan")
            @DefaultString("&fИгрок не в клане")
            fun playerNotClan(): String

            @ConfKey("player-bigger")
            @DefaultString("&fУ игрока выше уровень, чем у вас, поэтому вы не можете его кикнуть")
            fun playerBigger(): String

            @ConfKey("player-equals")
            @DefaultString("&fУ игрока такой же как у вас, поэтому вы не можете его кикнуть")
            fun playerEquals(): String

            @ConfKey("success")
            @DefaultString("&fВы успешно удалили игрока")
            fun success(): String

            @ConfKey("success-for-kicked-player")
            @DefaultString("&fВас удалили из клана")
            fun successForKickedPlayer(): String
        }

        @SubSection
        fun admin(): AdminCommand
        interface AdminCommand {
            @SubSection
            fun create(): CreateAdminCommand
            interface CreateAdminCommand {
                @ConfKey("use")
                @DefaultString("&fИспользуйте: /clan admin create name owner")
                fun use(): String

                @ConfKey("already-in-clan")
                @DefaultString("&cИгрок уже в клане")
                fun alreadyInClan(): String

                @ConfKey("clan-already-create")
                @DefaultString("&cКлан c таким именем уже создан")
                fun clanAlreadyCreate(): String

                @ConfKey("success")
                @DefaultString("&aУспешно добавлен клан")
                fun success(): String
            }

            @SubSection
            fun disband(): RemoveClanAdminCommand
            interface RemoveClanAdminCommand {
                @ConfKey("use")
                @DefaultString("&fИспользуйте: /clan admin remove name")
                fun use(): String

                @ConfKey("clan-not-create")
                @DefaultString("&cКлан c таким именем не существует")
                fun clanNotCreate(): String

                @ConfKey("success")
                @DefaultString("&aУспешно удален клан")
                fun success(): String
            }

            @SubSection
            fun reload(): ReloadAdminCommand
            interface ReloadAdminCommand {
                @ConfKey("success")
                @DefaultString("&aУспешно перезагружен плагин")
                fun success(): String
            }
        }
    }
}