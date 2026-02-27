package org.ReDiego0.saloonBetrayal.game

import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.ReDiego0.saloonBetrayal.game.character.PlayerCharacter
import org.ReDiego0.saloonBetrayal.game.role.Role
import org.ReDiego0.saloonBetrayal.manager.CharacterManager
import org.ReDiego0.saloonBetrayal.manager.RoleManager
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class Arena(
    val id: String,
    private val plugin: SaloonBetrayal,
    private val spawnLocations: List<Location>,
) {

    val players = mutableSetOf<Player>()
    var state: GameState = GameState.Waiting
        private set
    var isPrivate: Boolean = false

    val minPlayers = 4
    val maxPlayers = 7

    val playerRoles = mutableMapOf<Player, Role>()
    val playerCharacters = mutableMapOf<Player, PlayerCharacter>()
    private val roleManager = RoleManager()
    private val characterManager = CharacterManager()

    private var countdownTask: BukkitTask? = null

    init {
        require(spawnLocations.size >= maxPlayers) {
            "Fatal error in arena $id: Insufficient spawn points (${spawnLocations.size}/$maxPlayers). Check your config.yml."
        }
    }

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
        teleportPlayersToSeats()

        val assignedRoles = roleManager.assignRoles(players.toList())
        playerRoles.putAll(assignedRoles)

        val assignedCharacters = characterManager.assignCharacters(players.toList())
        playerCharacters.putAll(assignedCharacters)

        for (player in players) {
            val role = playerRoles[player] ?: continue
            val character = playerCharacters[player] ?: continue

            val maxHealth = character.baseHealth + role.healthModifier

            player.sendMessage("Role: ${role.namePath} | Character: ${character.namePath} | HP: $maxHealth")
        }
    }

    private fun teleportPlayersToSeats() {
        val playerList = players.toList()
        for (i in playerList.indices) {
            val player = playerList[i]
            val seatLocation = spawnLocations[i]

            player.teleport(seatLocation)

            player.inventory.clear()
            player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
            player.health = 20.0
            player.foodLevel = 20
        }
    }

    fun updateState(newState: GameState) {
        this.state = newState
    }
}