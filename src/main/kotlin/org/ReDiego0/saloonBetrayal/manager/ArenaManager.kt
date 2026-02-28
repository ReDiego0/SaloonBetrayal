package org.ReDiego0.saloonBetrayal.manager

import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.ReDiego0.saloonBetrayal.game.Arena
import org.ReDiego0.saloonBetrayal.game.GameState
import org.bukkit.Location
import org.bukkit.entity.Player

class ArenaManager(private val plugin: SaloonBetrayal) {

    private val arenas = mutableMapOf<String, Arena>()

    fun registerArena(arena: Arena) {
        arenas[arena.id] = arena
    }

    fun getArenaIds(): List<String> {
        return arenas.keys.toList()
    }

    fun getArena(player: Player): Arena? {
        return arenas.values.firstOrNull { it.players.contains(player) }
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

    fun loadArenas() {
        arenas.clear()
        val arenasSection = plugin.config.getConfigurationSection("arenas") ?: return

        for (arenaId in arenasSection.getKeys(false)) {
            val seats = mutableListOf<Location>()

            for (i in 1..7) {
                val loc = plugin.config.getLocation("arenas.$arenaId.seats.$i")
                if (loc != null) {
                    seats.add(loc)
                }
            }

            if (seats.size >= 7) {
                val arena = Arena(arenaId, plugin, seats)
                registerArena(arena)
                plugin.logger.info("Arena '$arenaId' cargada exitosamente con ${seats.size} asientos.")
            } else {
                plugin.logger.warning("La arena '$arenaId' no pudo cargarse. Tiene ${seats.size}/7 asientos configurados.")
            }
        }
    }

    fun saveSeat(arenaId: String, seatIndex: Int, location: Location) {
        plugin.config.set("arenas.$arenaId.seats.$seatIndex", location)
        plugin.saveConfig()
    }
}