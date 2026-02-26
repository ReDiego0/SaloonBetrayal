package org.ReDiego0.saloonBetrayal.command

import org.ReDiego0.saloonBetrayal.manager.ArenaManager
import org.ReDiego0.saloonBetrayal.manager.LanguageManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class SaloonCommand(
    private val arenaManager: ArenaManager,
    private val languageManager: LanguageManager
) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(languageManager.getMessage("errors.player_only"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(languageManager.getMessage("commands.help"))
            return true
        }

        when (args[0].lowercase()) {
            "join" -> handleJoin(sender, args)
            "private" -> handlePrivate(sender, args)
            else -> sender.sendMessage(languageManager.getMessage("errors.unknown_command"))
        }

        return true
    }

    private fun handleJoin(player: Player, args: Array<out String>) {
        if (args.size == 1) {
            val joined = arenaManager.joinRandom(player)
            if (joined) {
                player.sendMessage(languageManager.getMessage("arenas.join_random_success"))
            } else {
                player.sendMessage(languageManager.getMessage("arenas.no_available_arenas"))
            }
            return
        }

        val arenaId = args[1]
        val joined = arenaManager.joinSpecific(player, arenaId)

        if (joined) {
            player.sendMessage(languageManager.getMessage("arenas.join_specific_success", "arena" to arenaId))
        } else {
            player.sendMessage(languageManager.getMessage("arenas.join_specific_fail", "arena" to arenaId))
        }
    }

    private fun handlePrivate(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendMessage(languageManager.getMessage("commands.private_usage"))
            return
        }

        val partyPlayers = mutableListOf(player)

        for (i in 1 until args.size) {
            val target = Bukkit.getPlayerExact(args[i])
            if (target != null && target.isOnline) {
                partyPlayers.add(target)
            } else {
                player.sendMessage(languageManager.getMessage("errors.player_offline", "player" to args[i]))
                return
            }
        }

        val created = arenaManager.createPrivateParty(partyPlayers)

        if (created) {
            partyPlayers.forEach {
                it.sendMessage(languageManager.getMessage("arenas.private_created", "leader" to player.name))
            }
        } else {
            player.sendMessage(languageManager.getMessage("arenas.private_fail"))
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return listOf("join", "private").filter { it.startsWith(args[0].lowercase()) }
        }

        if (args.size == 2 && args[0].lowercase() == "join") {
            return arenaManager.getArenaIds().filter { it.startsWith(args[1], ignoreCase = true) }
        }

        return emptyList()
    }
}