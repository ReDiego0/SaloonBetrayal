package org.ReDiego0.saloonBetrayal.game

import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class Arena(val id: String, private val plugin: SaloonBetrayal) {

    val players = mutableSetOf<Player>()
    var state: GameState = GameState.Waiting
        private set
    var isPrivate: Boolean = false

    val minPlayers = 4
    val maxPlayers = 7

    private var countdownTask: BukkitTask? = null

    fun addPlayer(player: Player): Boolean {
        if (state !is GameState.Waiting && state !is GameState.Starting) return false
        if (players.size >= maxPlayers) return false

        players.add(player)
        checkStartConditions()
        return true
    }

    fun removePlayer(player: Player) {
        players.remove(player)
        if (state is GameState.Starting && players.size < minPlayers) {
            cancelCountdown()
        }
    }

    private fun checkStartConditions() {
        if (players.size >= minPlayers && state is GameState.Waiting) {
            startCountdown()
        }
    }

    private fun startCountdown() {
        state = GameState.Starting(30)
        countdownTask = object : BukkitRunnable() {
            var timeLeft = 30

            override fun run() {
                if (players.size == maxPlayers && timeLeft > 5) {
                    timeLeft = 5
                }

                state = GameState.Starting(timeLeft)

                if (timeLeft <= 0) {
                    startGame()
                    cancel()
                }
                timeLeft--
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    fun startPrivateGame(): Boolean {
        if (players.size < minPlayers) return false
        cancelCountdown()
        startGame()
        return true
    }

    private fun cancelCountdown() {
        countdownTask?.cancel()
        countdownTask = null
        state = GameState.Waiting
    }

    private fun startGame() {
        state = GameState.RoleSelection(players.toList())
    }
}