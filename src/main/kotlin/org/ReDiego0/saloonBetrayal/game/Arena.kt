package org.ReDiego0.saloonBetrayal.game

import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.ReDiego0.saloonBetrayal.game.card.Deck
import org.ReDiego0.saloonBetrayal.game.card.GameCard
import org.ReDiego0.saloonBetrayal.game.card.MustangCard
import org.ReDiego0.saloonBetrayal.game.card.ScopeCard
import org.ReDiego0.saloonBetrayal.game.card.WeaponCard
import org.ReDiego0.saloonBetrayal.game.character.PlayerCharacter
import org.ReDiego0.saloonBetrayal.game.role.Role
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Arena(
    val id: String,
    private val plugin: SaloonBetrayal,
    private val spawnLocations: List<Location>,
) {

    lateinit var deck: Deck
        private set
    lateinit var turnManager: GameTurnManager
        private set

    val players = mutableListOf<Player>()
    var state: GameState = GameState.Waiting
        private set

    var isPrivate: Boolean = false

    val deadPlayers = mutableSetOf<Player>()

    val minPlayers = 4
    val maxPlayers = 7

    val playerRoles = mutableMapOf<Player, Role>()
    val playerCharacters = mutableMapOf<Player, PlayerCharacter>()
    val playerEquipment = mutableMapOf<Player, MutableList<GameCard>>()

    private var countdownTask: BukkitTask? = null

    init {
        require(spawnLocations.size >= maxPlayers) {
            "Fatal error in arena $id: Insufficient spawn points (${spawnLocations.size}/$maxPlayers). Check your config.yml."
        }
    }

    fun addPlayer(player: Player): Boolean {
        if (state !is GameState.Waiting && state !is GameState.Starting) return false
        if (players.size >= maxPlayers) return false
        if (players.contains(player)) return false

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

        val assignedRoles = SaloonBetrayal.instance.roleManager.assignRoles(players.toList())
        playerRoles.putAll(assignedRoles)

        val assignedCharacters = SaloonBetrayal.instance.characterManager.assignCharacters(players.toList())
        playerCharacters.putAll(assignedCharacters)

        for (player in players) {
            val role = playerRoles[player] ?: continue
            val character = playerCharacters[player] ?: continue

            val maxHealth = character.baseHealth + role.healthModifier

            player.sendMessage("Role: ${role.namePath} | Character: ${character.namePath} | HP: $maxHealth")
            SaloonBetrayal.instance.displayManager.setupDisplayForPlayer(player, this, "ABOVE")
            playerEquipment[player] = mutableListOf()
        }

        deck = Deck()
        deck.initializeBaseDeck()

        turnManager = GameTurnManager(this, deck)
        turnManager.initialize(players.toList())
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

    fun getPlayerMaxHealth(player: Player): Int {
        val role = playerRoles[player] ?: return 4
        val character = playerCharacters[player] ?: return 4
        return character.baseHealth + role.healthModifier
    }

    fun getPlayerCurrentHealth(player: Player): Int {
        return (player.health / 2).toInt()
    }

    fun takeDamage(target: Player, amount: Int = 1) {
        if (deadPlayers.contains(target)) return
        val currentHealth = (target.health / 2).toInt()
        val newHealth = currentHealth - amount

        if (newHealth <= 0) {
            handlePlayerDeath(target)
        } else {
            target.health = (newHealth * 2).toDouble()
            SaloonBetrayal.instance.displayManager.updateDisplay(target, this)
            target.playSound(target.location, org.bukkit.Sound.ENTITY_PLAYER_HURT, 1f, 1f)
        }
    }

    private fun handlePlayerDeath(player: Player) {
        deadPlayers.add(player)
        players.remove(player)
        val role = playerRoles[player]
        val languageManager = SaloonBetrayal.instance.languageManager
        val deathMsg = languageManager.getMessage("messages.death_reveal", "player" to player.name)

        for (p in players.plus(deadPlayers)) {
            p.sendMessage(deathMsg)
        }

        turnManager.handlePlayerDeath(player)

        player.gameMode = org.bukkit.GameMode.CREATIVE
        player.inventory.clear()
        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false, false))
        val skull = org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD)
        val skullMeta = skull.itemMeta as org.bukkit.inventory.meta.SkullMeta
        skullMeta.owningPlayer = player
        skull.itemMeta = skullMeta
        player.inventory.helmet = skull

        val leaveItem = org.bukkit.inventory.ItemStack(org.bukkit.Material.RED_BED)
        val leaveMeta = leaveItem.itemMeta
        leaveMeta?.displayName(languageManager.getMessage("items.leave_spectate"))
        leaveItem.itemMeta = leaveMeta
        player.inventory.setItem(8, leaveItem)

        SaloonBetrayal.instance.displayManager.removeDisplay(player)

        checkWinConditions()
    }

    private fun getBaseDistance(player1: Player, player2: Player): Int {
        val index1 = players.indexOf(player1)
        val index2 = players.indexOf(player2)
        if (index1 == -1 || index2 == -1) return 99

        val diff = abs(index1 - index2)
        return min(diff, players.size - diff)
    }

    fun getDistance(viewer: Player, target: Player): Int {
        var distance = getBaseDistance(viewer, target)

        val targetEq = playerEquipment[target] ?: emptyList()
        if (targetEq.any { it.baseCard is MustangCard }) distance += 1
        if (playerCharacters[target] == PlayerCharacter.PaulRegret) distance += 1

        val viewerEq = playerEquipment[viewer] ?: emptyList()
        if (viewerEq.any { it.baseCard is ScopeCard }) distance -= 1

        return max(1, distance)
    }

    fun getWeaponRange(player: Player): Int {
        val equipment = playerEquipment[player] ?: emptyList()
        val weapon = equipment.firstOrNull { it.baseCard is WeaponCard }?.baseCard as? WeaponCard

        return weapon?.range ?: 1
    }

    fun canSee(viewer: Player, target: Player): Boolean {
        return getDistance(viewer, target) <= getWeaponRange(viewer)
    }

    fun canPlayMultipleBangs(player: Player): Boolean {
        if (playerCharacters[player] == PlayerCharacter.WillyTheKid) return true

        val equipment = playerEquipment[player] ?: emptyList()
        return equipment.any { it.baseCard.id == "volcanic" }
    }

    private fun checkWinConditions() {
        val sheriffAlive = players.any { playerRoles[it] == Role.Sheriff }

        val outlawsAliveCount = players.count { playerRoles[it] == Role.Outlaw }
        val renegadesAliveCount = players.count { playerRoles[it] == Role.Renegade }

        if (!sheriffAlive) {
            if (players.size == 1 && renegadesAliveCount == 1) {
                endGame("RENEGADE")
            } else {
                endGame("OUTLAWS")
            }
            return
        }

        if (outlawsAliveCount == 0 && renegadesAliveCount == 0) {
            endGame("LAW")
        }
    }

    private fun endGame(winningTeam: String) {
        val languageManager = SaloonBetrayal.instance.languageManager
        val winMessage = when (winningTeam) {
            "RENEGADE" -> languageManager.getMessage("messages.win_renegade")
            "OUTLAWS" -> languageManager.getMessage("messages.win_outlaws")
            "LAW" -> languageManager.getMessage("messages.win_law")
            else -> null
        }

        if (winMessage != null) {
            val allPlayers = players.plus(deadPlayers)
            allPlayers.forEach { p ->
                val subtitle = languageManager.getMessage("messages.win_subtitle")
                p.showTitle(net.kyori.adventure.title.Title.title(winMessage, subtitle))
                p.sendMessage(winMessage)
                p.playSound(p.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            }
        }

        object : BukkitRunnable() {
            override fun run() {
                resetArena()
            }
        }.runTaskLater(plugin, 200L)
    }

    private fun resetArena() {
        val allPlayers = players.plus(deadPlayers)

        // TODO: Enviar a los jugadores pal lobby
        allPlayers.forEach { p ->
            p.inventory.clear()
            p.activePotionEffects.forEach { p.removePotionEffect(it.type) }
            p.health = 20.0
            p.foodLevel = 20
            p.gameMode = org.bukkit.GameMode.SURVIVAL
            SaloonBetrayal.instance.displayManager.removeDisplay(p)
        }

        players.clear()
        deadPlayers.clear()
        playerRoles.clear()
        playerCharacters.clear()
        playerEquipment.clear()
        isPrivate = false
        state = GameState.Waiting
    }
}