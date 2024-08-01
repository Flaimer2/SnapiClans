package ru.snapix.clan

import com.alonsoaliaga.alonsolevels.api.AlonsoLevelsAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import ru.snapix.clan.api.*
import ru.snapix.clan.caches.Clans
import ru.snapix.library.SnapiLibrary
import ru.snapix.library.bukkit.panel.Item
import ru.snapix.library.bukkit.panel.dsl.Material
import ru.snapix.library.bukkit.panel.dsl.generatorPanel
import ru.snapix.library.bukkit.panel.dsl.panel
import ru.snapix.library.bukkit.panel.nextPage
import ru.snapix.library.bukkit.panel.prevPage
import ru.snapix.library.bukkit.utils.cancelNextChat
import ru.snapix.library.bukkit.utils.hasMoney
import ru.snapix.library.bukkit.utils.nextChat
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.library.network.player.OnlineNetworkPlayer
import ru.snapix.library.utils.message
import ru.snapix.library.utils.toDate
import ru.snapix.profile.PanelStorage.getStatisticInt
import ru.snapix.snapicooperation.PanelStorage.friendMenu
import sun.nio.ch.Net
import kotlin.time.Duration.Companion.seconds

object PanelStorage {
    fun clan(player: Player, backProfile: Boolean = false) {
        val user = ClanApi.user(player.name)
        val clan = user?.clan()
        if (clan == null) {
            nullClan(player, backProfile)
        } else if (clan.owner.equals(player.name, ignoreCase = true)) {
            controlClan(player, backProfile)
        } else {
            userList(player, backProfile)
        }
    }

    fun nullClan(player: Player, backProfile: Boolean = false) {
        panel(player) {
            title = "Кланы"
            update = 1.seconds

            layout {
                - "         "
                - "         "
                - "   F C   "
                - "         "
                - "         "
                - "  O B N  "
            }

            items {
                'F' {
                    name = "&aНайти клан"
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFjZTUxNTBiZTdiNjJkYjAyYjlhZmIwNTRmMGQwZWJiYjhhY2I4MzNjMzQ3YWJmZDliNzgzY2EwYTllNTY4MiJ9fX0="
                    lore {
                        - "&fНайдите клан и присоединяйтесь"
                        - "&fк сообществу игроков сервера!"
                        - ""
                        - "&fВсего кланов: &a{clan_amount}"
                        - ""
                        - "{find_clan_action}"
                    }
                    actions {
                        val level = AlonsoLevelsAPI.getLevel(player.uniqueId)
                        if (level >= 7) {
                            clans(player, true)
                        } else {
                            player.message("&fНеобходим &a7 уровень &fдля вступления в клан!")
                        }
                    }
                }
                'C' {
                    name = "&aСоздать клан"
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBiNTVmNzQ2ODFjNjgyODNhMWMxY2U1MWYxYzgzYjUyZTI5NzFjOTFlZTM0ZWZjYjU5OGRmMzk5MGE3ZTcifX19"
                    lore {
                        - "&fСоздайте собственный клан,"
                        - "&fпригласите друзей, играйте"
                        - "&fи управляйте им вместе!"
                        - "&f"
                        - "&fЦена: &a50000$"
                        - "&f"
                        - "{create_clan_action}"
                    }
                    actions {
                        val level = AlonsoLevelsAPI.getLevel(player.uniqueId)
                        if (level < 14) {
                            player.message("&fНеобходим &a14 уровень &fдля вступления в клан!")
                        } else if (!player.hasMoney(50000)) {
                            player.message("&fУ вас &cнедостаточно &fденег для создания клана")
                        } else {
                            player.message("&fНапишите &aназвание &fклана в чат. Оно должно содержать больше &a3 &fи меньше &a16 &fсимволов. Можно использовать &aлатинские символы &fи &aцифры&f.")
                            player.message("&fДля &cотмены &fсоздания клана, напишите: &aотмена&f или &aотменить")
                            player.cancelNextChat()
                            player.closeInventory()
                            player.nextChat {
                                val input = it
                                if (input.equals("отмена", ignoreCase = true) || input.equals("cancel", ignoreCase = true) || input.equals("отменить", ignoreCase = true)) {
                                    player.cancelNextChat()
                                    player.message("&fВы &cотменили &fсоздание клана!")
                                } else {
                                    Bukkit.dispatchCommand(player, "clan create $it")
                                }
                            }
                        }
                    }
                }
                'O' {
                    name = "&aОнлайн в клане"
                    material = Material.PAPER
                    lore {
                        -""
                        -"&fОнлайн участников: TODO&a{online_friend}"
                        -"&fОнлайн друзей в клане: TODO&a{online}"
                    }
                }
                'N' {
                    name = "&aТребования для кланов"
                    material = Material.REDSTONE
                    lore {
                        - "&fДля присоединения к клану:"
                        - "{join_clan_level_require}"
                        - ""
                        - "&fДля создания клана:"
                        - "{create_clan_level_require}"
                        - "{create_clan_money_require}"
                    }
                }
                'B' {
                    name = "&aВернуться назад"
                    material = Material.ARROW
                    condition { backProfile }
                    actions {
                        ru.snapix.profile.PanelStorage.profile(player)
                    }
                }
            }

            replacements {
                - ("clan_amount" to { ClanApi.clans().size })
                - ("find_clan_action" to {
                    val level = AlonsoLevelsAPI.getLevel(player.uniqueId)
                    if (level >= 7) {
                        "&aНажмите, чтобы открыть список"
                    } else {
                        "&cДля присоединения к кланам необходим 7 уровень"
                    }
                })
                - ("create_clan_level_require" to {
                    val level = AlonsoLevelsAPI.getLevel(player.uniqueId)
                    if (level >= 14) {
                        "&fУровень: &a14"
                    } else {
                        "&7&mУровень: 14"
                    }
                })
                - ("create_clan_money_require" to {
                    if (player.hasMoney(50000)) {
                        "&fЦена: &a50000$"
                    } else {
                        "&7&mЦена: 50000$"
                    }
                })
                - ("create_clan_action" to {
                    val level = AlonsoLevelsAPI.getLevel(player.uniqueId)
                    if (level <= 14) {
                        "&cДля создания клана необходим 14 уровень"
                    } else if (!player.hasMoney(50000)) {
                        "&cУ вас недостаточно денег"
                    } else {
                        "&aНажмите, чтобы создать клан"
                    }
                })
                - ("join_clan_level_require" to {
                    val level = AlonsoLevelsAPI.getLevel(player.uniqueId)
                    if (level >= 7) {
                        "&fУровень: &a7"
                    } else {
                        "&7&mУровень: 7"
                    }
                })
                - ("online" to { SnapiLibrary.getOnlinePlayers().size })
                - ("online_friend" to { ru.snapix.snapicooperation.api.User[player.name].friends.size })
            }
        }
    }

    fun controlClan(player: Player, backProfile: Boolean = false) {
        panel(player) {
            title = "Управление кланом"
            update = 1.seconds

            layout {
                - "         "
                - "         "
                - " P  T  D "
                - "         "
                - "         "
                - "  O B I  "
            }

            items {
                'P' {
                    name = "&aСписок участников"
                    material = Material.BOOK
                    lore {
                        - "&fПросмотрите всех участников"
                        - "&fклана и их текущий статус"
                        - ""
                        - "&fУчастников: &a%clan_members_size%/%clan_max_members%"
                        - ""
                        - "&aНажмите, чтобы открыть список"
                    }
                    actions {
                        userList(player)
                    }
                }
                'T' {
                    name = "&aТег клана"
                    material = Material.OAK_SIGN
                    lore {
                        - "&fДобавьте тег для клана,"
                        - "&fчтобы все видели его в чате!"
                        - "&f"
                        - "&fМаксимальный размер тега: &a5 символов"
                        - "&f"
                        - "&fЦена: &a35000$"
                        - "&f"
                        - "{tag_clan_action}"
                    }
                    actions {
                        if (player.hasMoney(35000)) {
                            player.message("&cУ вас недостаточно денег")
                        } else {
                            player.message("&fНапишите &aтег &fклана в чат. Оно должно содержать больше &a3 &fи меньше &a5 &fсимволов. Можно использовать &aлатинские символы&f, &aцифры&f, &a—&f, &a< &fи &a>")
                            player.cancelNextChat()
                            player.closeInventory()
                            player.nextChat {
                                val input = it
                                if (input.equals("отмена", ignoreCase = true) || input.equals("cancel", ignoreCase = true) || input.equals("отменить", ignoreCase = true)) {
                                    player.cancelNextChat()
                                    player.message("&fВы &cотменили &fдобавление тега клану!")
                                } else {
                                    Bukkit.dispatchCommand(player, "clan tag $it")
                                }
                            }
                        }
                    }
                }
                'D' {
                    name = "&cУдалить клан"
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQxYTNjOTY1NjIzNDg1MjdkNTc5OGYyOTE2MDkyODFmNzJlMTZkNjExZjFhNzZjMGZhN2FiZTA0MzY2NSJ9fX0="
                    lore {
                        -"&fБезвозвратно удалить ваш"
                        -"&fклан и всех его участников"
                        -""
                        -"&aНажмите, чтобы удалить"
                    }
                    actions {
                        Bukkit.dispatchCommand(player, "clan disband")
                    }
                }
                'O' {
                    name = "&aОнлайн игроков"
                    material = Material.PAPER
                    lore {
                        -""
                        -"&fОнлайн друзей: &a{online_friend}"
                        -"&fОнлайн на сервере: &a{online}"
                    }
                }
                'I' {
                    name = "&aИнформация о клане"
                    head = ClanApi.user(player.name)?.clan()?.owner ?: "steve"
                    lore {
                        - "&f"
                        - "&fНазвание клана: &a%clan_name%"
                        - "&fТег клана: %clan_tag%"
                        - "&fУчастников: &a%clan_members_size%/%clan_max_members%"
                        - "&fДата создания: &a%clan_date_creation_without_hour%"
                    }
                }
                'B' {
                    name = "&a"
                    material = Material.ARROW
                    condition { backProfile }
                    actions {
                        ru.snapix.profile.PanelStorage.profile(player)
                    }
                }
            }

            replacements {
                - ("tag_clan_action" to {
                    if (!player.hasMoney(35000)) {
                        "&cУ вас недостаточно денег"
                    } else {
                        "&aНажмите, чтобы добавить тег"
                    }
                })
                - ("online" to { SnapiLibrary.getOnlinePlayers().size })
                - ("online_friend" to { ru.snapix.snapicooperation.api.User[player.name].friends.size })
            }
        }
    }

    fun userList(player: Player, backProfile: Boolean = false) {
        generatorPanel<User>(player) {
            title = "Клан"

            generatorSource {
                val user = ClanApi.user(player.name)
                val clan = user?.clan()
                val list = clan?.users()
                if (list == null) {
                    nullClan(player)
                    emptyList()
                } else {
                    list
                }
            }
            generatorOutput = {
                val networkPlayer = SnapiLibrary.getPlayer(it.name)
                val clan = it.clan()
                if (clan == null) {
                    nullClan(player)
                    Item()
                } else {
                    Item(
                        name = "&a${it.name}",
                        head = it.name,
                        lore = listOf(
                            "",
                            "&fРоль: &a${it.role.displayName}",
                            "&fСтатус: ${if (networkPlayer.isOnline()) "&aОнлайн" else "&cОфлайн"}",
                            if (networkPlayer.isOnline()) "&fСейчас находится: &a${networkPlayer.getCurrentServer()?.name ?: "Где-то в переходе..."}" else "\\",
                            "&fВступил: &a${it.dateJoin.toDate("dd/MM/yyyy")}",
                            "",
                            "{generator_action}"
                        ),
                        clickAction = {
                            val user = ClanApi.user(player.name) ?: return@Item
                            if (user.role == ClanRole.DEFAULT) {
                                ru.snapix.profile.PanelStorage.otherProfile(player, SnapiLibrary.getPlayer(it.name), backClan = true)
                            } else {
                                when (type) {
                                    ClickType.LEFT -> {
                                        Bukkit.dispatchCommand(player, "clan role increase ${it.name}")
                                    }
                                    ClickType.SHIFT_LEFT -> {
                                        Bukkit.dispatchCommand(player, "clan role decrease ${it.name}")
                                    }
                                    ClickType.MIDDLE -> {
                                        Bukkit.dispatchCommand(player, "clan remove ${it.name}")
                                    }
                                    ClickType.RIGHT -> {
                                        ru.snapix.profile.PanelStorage.otherProfile(player, SnapiLibrary.getPlayer(it.name), backClan = true)
                                    }
                                    else -> {}
                                }
                            }
                        }
                    )
                }
            }
            comparator = compareBy<User> { it.role.weight }.thenBy { it.name }

            layout {
                - "FFFFFFFFF"
                - "F       F"
                - "F       F"
                - "F       F"
                - "FFFFFFFFF"
                - "FFIFBFAFF"
            }

            items {
                'F' {
                    material = Material.AIR
                }
                'I' {
                    name = "&aИнформация о клане"
                    material = Material.PAPER
                    lore {
                        - "&f"
                        - "&fНазвание клана: &a%clan_name%"
                        - "&fТег клана: %clan_tag%"
                        - "&fУчастников: &a%clan_members_size%/%clan_max_members%"
                        - "&fДата создания: &a%clan_date_creation_without_hour%"
                    }
                }
                'A' {
                    name = if (ClanApi.user(player.name)?.hasPermission(ClanPermission.INVITE) == true) "&aДобавить участника" else "&cВыйти из клана"
                    head = if (ClanApi.user(player.name)?.hasPermission(ClanPermission.INVITE) == true) "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBiNTVmNzQ2ODFjNjgyODNhMWMxY2U1MWYxYzgzYjUyZTI5NzFjOTFlZTM0ZWZjYjU5OGRmMzk5MGE3ZTcifX19" else "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQxYTNjOTY1NjIzNDg1MjdkNTc5OGYyOTE2MDkyODFmNzJlMTZkNjExZjFhNzZjMGZhN2FiZTA0MzY2NSJ9fX0="
                    lore {
                        - if (ClanApi.user(player.name)?.hasPermission(ClanPermission.INVITE) == true) {
                            "&fВы можете отправить приглашение\n&fигроку сервера в ваш клан!" +
                                    "\n" +
                                    "\n" +
                                    "&fОнлайн друзей: &a{online_friend}" +
                                    "\n" +
                                    "&fОнлайн на сервере: &a{online}" +
                                    "\n" +
                                    "\n" +
                                    "&aНажмите, чтобы открыть список"
                        } else {
                            "&fПокинуть этот клан и" +
                                    "\n" +
                                    "&fпрекратить участие в нём" +
                                    "\n" +
                                    "\n" +
                                    "&aНажмите, чтобы выйти"
                        }
                    }
                    actions {
                        if (ClanApi.user(player.name)?.hasPermission(ClanPermission.INVITE) == true) {
                            playerList(player)
                        } else {
                            Bukkit.dispatchCommand(player, "clan leave")
                        }
                    }
                }
                'B' {
                    name = "&aВернуться назад"
                    material = Material.ARROW
                    condition { backProfile }
                    actions {
                        ru.snapix.profile.PanelStorage.profile(player)
                    }
                }
            }

            replacements {
                - ("generator_action" to {
                    val user = ClanApi.user(player.name)
                    if (user == null) {
                        "&aНажмите ПКМ, чтобы открыть профиль"
                    } else if (user.role == ClanRole.DEFAULT) {
                        "&aНажмите, чтобы открыть профиль"
                    } else {
                        buildString {
                            if (user.hasPermission(ClanPermission.ROLE_INCREASE)) {
                                appendLine("&aНажмите ЛКМ, чтобы повысить игрока")
                            }
                            if (user.hasPermission(ClanPermission.ROLE_DECREASE)) {
                                appendLine("&aНажмите Shift + ЛКМ, чтобы понизить игрока")
                            }
                            if (user.hasPermission(ClanPermission.KICK)) {
                                appendLine("&aНажмите СКМ, чтобы удалить участника")
                            }
                            append("&aНажмите ПКМ, чтобы открыть профиль")
                        }
                    }
                })
                - ("online" to { SnapiLibrary.getOnlinePlayers().size })
                - ("online_friend" to { ru.snapix.snapicooperation.api.User[player.name].friends.size })
            }
        }
    }

    fun clans(player: Player, find: Boolean = false) {
        generatorPanel<Clan>(player) {
            title = "Список кланов"

            generatorSource {
                Clans.values()
            }
            generatorOutput = {
                Item(
                    name = "&a${it.name}",
                    head = it.owner,
                    lore = listOf(
                        "",
                        "&fНазвание клана: &a${it.name}",
                        "&fЛидер клана: &a${it.owner}",
                        "&fУчастников: &a${it.users().size}/${it.maxMembers}",
                        "&fДата создания: &a${it.dateCreation.toDate("dd/MM/yyyy")}",
                        "&f",
                        "&aНажмите, чтобы открыть клан",
                    ),
                    clickAction = {
                        otherClan(player, it)
                    }
                )
            }
            filter { if (find) it.users().size < it.maxMembers else true }
            comparator = compareBy { it.name }

            layout {
                - "FFFFFFFFF"
                - "F       F"
                - "F       F"
                - "F       F"
                - "FFFFFFFFF"
                - "FFFFBFFFF"
            }

            items {
                'F' {
                    material = Material.AIR
                }
                'B' {
                    name = "&aВернуться назад"
                    material = Material.ARROW
                    condition { find }
                    actions {
                        nullClan(player)
                    }
                }
            }
        }
    }

    fun otherClan(player: Player, clan: Clan, otherPlayer: NetworkPlayer? = null) {
        generatorPanel<User>(player) {
            title = "Клан"

            generatorSource { clan.users() }
            generatorOutput = {
                val networkPlayer = SnapiLibrary.getPlayer(it.name)
                Item(
                    name = "&a${it.name}",
                    head = it.name,
                    lore = listOf(
                        "",
                        "&fРоль: &a${it.role.displayName}",
                        "&fСтатус: ${if (networkPlayer.isOnline()) "&aОнлайн" else "&cОфлайн"}",
                        if (networkPlayer.isOnline()) "&fСейчас находится: &a${networkPlayer.getCurrentServer()?.name ?: "Где-то в переходе..."}" else "\\",
                        "&fВступил: &a${it.dateJoin.toDate("dd/MM/yyyy")}",
                        "",
                        "&aНажмите, чтобы открыть профиль"
                    ),
                    clickAction = {
                        ru.snapix.profile.PanelStorage.otherProfile(
                            player,
                            SnapiLibrary.getPlayer(it.name),
                            backClan = true
                        )
                    }
                )
            }
            comparator = compareBy<User> { it.role.weight }.thenBy { it.name }

            layout {
                - "FFFFFFFFF"
                - "F       F"
                - "F       F"
                - "F       F"
                - "FFFFFFFFF"
                - "FFIFFFAFF"
            }

            items {
                'F' {
                    material = Material.AIR
                }
                'I' {
                    name = "&aИнформация о клане"
                    material = Material.PAPER
                    lore {
                        - "&f"
                        - "&fНазвание клана: &a${clan.name}"
                        - "&fТег клана: ${clan.tag ?: "&cНет"}"
                        - "&fУчастников: &a${clan.users().size}/${clan.maxMembers}"
                        - "&fДата создания: &a${clan.dateCreation.toDate("dd/MM/yyyy")}"
                    }
                }
                'A' {
                    name = "&aСкоро..."
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM1OWQ5MTI3NzI0MmZjMDFjMzA5YWNjYjg3YjUzM2YxOTI5YmUxNzZlY2JhMmNkZTYzYmY2MzVlMDVlNjk5YiJ9fX0="
                    lore {
                        - "&fЭта функция ещё недоступна"
                    }
                }
                'B' {
                    name = "&aВернуться назад"
                    material = Material.ARROW
                    condition { otherPlayer != null }
                    actions {
                        if (otherPlayer != null) {
                            ru.snapix.profile.PanelStorage.otherProfile(player, otherPlayer)
                        }
                    }
                }
            }
        }
    }

    fun playerList(player: Player) {
        generatorPanel<NetworkPlayer>(player) {
            title = "Список игроков"

            generatorSource {
                SnapiLibrary.getOnlinePlayers().toMutableList().filter { ClanApi.user(it.getName())?.clan() == null }
            }

            generatorOutput = {
                Item(
                    name = "&a${it.getName()}",
                    head = it.getName(),
                    lore = listOf(
                        "&fВы можете отправить запрос на",
                        "&fвступление в клан этому игроку",
                        "",
                        "&fУровень: &a${getStatisticInt(it, "ALONSOLEVELS_LASTLEVEL")}",
                        "&fЛюбимый режим: {favourite_game}",
                        "",
                        "&aНажмите, чтобы отправить",
                    ),
                    clickAction = {
                        Bukkit.dispatchCommand(player, "clan invite ${it.getName()}")
                        userList(player)
                    }
                )
            }
            comparator = compareBy { it.getName() }

            layout {
                -"FFFFFFFFF"
                -"F       F"
                -"F       F"
                -"F       F"
                -"FFFFFFFFF"
                -"PFFFRFFFN"
            }

            items {
                'F' {
                    material = Material.AIR
                }
                'R' {
                    material = Material.ARROW
                    name = "&aВернуться"
                    actions {
                        userList(player)
                    }
                }
                'P' {
                    material = Material.ARROW
                    name = "&aПредыдущая страница"
                    condition { prevPage() }
                    actions {
                        prevPage()
                    }
                }
                'N' {
                    material = Material.ARROW
                    name = "&aСледующая страница"
                    condition { nextPage() }
                    actions {
                        nextPage()
                    }
                }
            }

            replacements {
                - ("favourite_game" to {
                    val networkPlayer = OnlineNetworkPlayer(player.name)
                    val map = mapOf(
                        "skywars" to getStatisticInt(networkPlayer, "skywars_played"),
                        "bedwars" to getStatisticInt(
                            networkPlayer,
                            "bedwars_gamesplayed"
                        ),
                        "murdermystery" to getStatisticInt(networkPlayer, "murdermystery_games_played"),
                        "thebridge" to getStatisticInt(
                            networkPlayer,
                            "thebridge_gamesplayed"
                        )
                    ).filter { it.value > 0 }
                    val max = map.maxByOrNull { it.value }
                    when (max?.key) {
                        "skywars" -> "&bSkyWars"
                        "bedwars" -> "&cBedWars"
                        "murdermystery" -> "&eMurderMystery"
                        "thebridge" -> "&9TheBridge"
                        else -> "&cНет"
                    }
                })
            }
        }
    }

    fun getStatisticInt(player: NetworkPlayer, placeholder: String): Int {
        return try {
            player.getStatistics()[placeholder]!!.invoke().toString().toInt()
        } catch (_: Exception) {
            0
        }
    }
}