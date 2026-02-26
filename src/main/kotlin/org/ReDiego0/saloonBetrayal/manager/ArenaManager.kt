package org.ReDiego0.saloonBetrayal.manager

import org.ReDiego0.saloonBetrayal.game.Arena
import org.ReDiego0.saloonBetrayal.game.GameState
import org.bukkit.entity.Player

class ArenaManager {

    private val arenas = mutableMapOf<String, Arena>()

    fun registerArena(arena: Arena) {
        arenas[arena.id] = arena
    }

    fun getArenaIds(): List<String> {
        return arenas.keys.toList()
    }

    fun joinRandom(player: Player): Boolean {
        val availableArena = arenas.values
            .filter { !it.isPrivate && (it.state is GameState.Waiting || it.state is GameState.Starting) }
            .filter { it.players.size < it.maxPlayers }
            .maxByOrNull { it.players.size }

        return availableArena?.addPlayer(player) ?: false
    }

    fun joinSpecific(player: Player, arenaId: String): Boolean {
        val arena = arenas[arenaId] ?: return false
        if (arena.isPrivate) return false

        return arena.addPlayer(player)
    }

    fun createPrivateParty(players: List<Player>): Boolean {
        val emptyArena = arenas.values.firstOrNull {
            it.players.isEmpty() && it.state is GameState.Waiting
        } ?: return false

        emptyArena.isPrivate = true
        players.forEach { emptyArena.addPlayer(it) }

        return emptyArena.startPrivateGame()
    }
}