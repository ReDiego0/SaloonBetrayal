package org.ReDiego0.saloonBetrayal.game

import org.ReDiego0.saloonBetrayal.game.card.Deck
import org.bukkit.entity.Player

class GameTurnManager(
    private val arena: Arena,
    private val deck: Deck
) {

    private var activePlayers = listOf<Player>()
    private var currentPlayerIndex = 0

    var hasPlayedBangThisTurn: Boolean = false
        private set

    fun initialize(players: List<Player>) {
        activePlayers = players.toList()
        currentPlayerIndex = (0 until activePlayers.size).random()
        startTurn(activePlayers[currentPlayerIndex])
    }

    private fun startTurn(player: Player) {
        hasPlayedBangThisTurn = false
        arena.updateState(GameState.Playing(player, TurnPhase.Draw))

        val equipment = arena.playerEquipment[player] ?: emptyList()
        val dynamiteCard = equipment.firstOrNull { it.baseCard.id == "dynamite" }
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager

        if (dynamiteCard != null) {
            player.sendMessage(lm.getMessage("messages.dynamite_check"))

            org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.drawCheckManager.requestDrawCheck(
                player = player,
                arena = arena,
                reasonPath = "reasons.dynamite",
                condition = { suit, rank -> suit == org.ReDiego0.saloonBetrayal.game.card.Suit.SPADES && rank.numericValue in 2..9 }
            ) { explodes ->
                arena.playerEquipment[player]?.remove(dynamiteCard)

                if (explodes) {
                    player.sendMessage(lm.getMessage("messages.dynamite_exploded"))
                    arena.deck.discard(dynamiteCard)
                    arena.takeDamage(player, 3)

                    if (!arena.deadPlayers.contains(player)) {
                        checkJailAndProceed(player)
                    }
                } else {
                    player.sendMessage(lm.getMessage("messages.dynamite_passed"))
                    val nextPlayer = activePlayers[(currentPlayerIndex + 1) % activePlayers.size]
                    arena.playerEquipment[nextPlayer]?.add(dynamiteCard)

                    checkJailAndProceed(player)
                }
            }
        } else {
            checkJailAndProceed(player)
        }
    }

    private fun checkJailAndProceed(player: Player) {
        val equipment = arena.playerEquipment[player] ?: emptyList()
        val jailCard = equipment.firstOrNull { it.baseCard.id == "jail" }
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager

        if (jailCard != null) {
            arena.playerEquipment[player]?.remove(jailCard)
            arena.deck.discard(jailCard)

            player.sendMessage(lm.getMessage("messages.jail_check"))

            org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.drawCheckManager.requestDrawCheck(
                player = player,
                arena = arena,
                reasonPath = "reasons.jail",
                condition = { suit, _ -> suit == org.ReDiego0.saloonBetrayal.game.card.Suit.HEARTS }
            ) { isSuccess ->
                if (isSuccess) {
                    player.sendMessage(lm.getMessage("messages.jail_escaped"))
                    processDrawPhase(player)
                } else {
                    player.sendMessage(lm.getMessage("messages.jail_failed"))
                    requestTurnEnd(player)
                }
            }
        } else {
            processDrawPhase(player)
        }
    }

    private fun processDrawPhase(player: Player) {
        val card1 = deck.draw()
        val card2 = deck.draw()

        val languageManager = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        player.inventory.addItem(org.ReDiego0.saloonBetrayal.game.card.CardMapper.run { card1.toItemStack(languageManager) })
        player.inventory.addItem(org.ReDiego0.saloonBetrayal.game.card.CardMapper.run { card2.toItemStack(languageManager) })

        arena.updateState(GameState.Playing(player, TurnPhase.Action))
    }

    fun registerBangPlayed() {
        hasPlayedBangThisTurn = true
    }

    fun requestTurnEnd(player: Player): Boolean {
        val currentState = arena.state
        if (currentState !is GameState.Playing || currentState.currentPlayer != player) return false
        if (currentState.turnPhase == TurnPhase.Discard) return true

        arena.updateState(GameState.Playing(player, TurnPhase.Discard))

        val currentCards = player.inventory.contents.count { it != null && org.ReDiego0.saloonBetrayal.game.card.CardMapper.run { it.getCardId() } != null }
        val currentHealth = arena.getPlayerCurrentHealth(player)

        if (currentCards <= currentHealth) {
            passTurnToNext()
        }

        return true
    }

    fun passTurnToNext() {
        if (activePlayers.isEmpty()) return

        currentPlayerIndex = (currentPlayerIndex + 1) % activePlayers.size
        startTurn(activePlayers[currentPlayerIndex])
    }

    fun handlePlayerDeath(deadPlayer: Player) {
        val indexToRemove = activePlayers.indexOf(deadPlayer)
        if (indexToRemove == -1) return

        val mutablePlayers = activePlayers.toMutableList()
        mutablePlayers.removeAt(indexToRemove)
        activePlayers = mutablePlayers.toList()

        if (activePlayers.isEmpty()) return

        if (indexToRemove < currentPlayerIndex) {
            currentPlayerIndex--
        } else if (indexToRemove == currentPlayerIndex) {
            currentPlayerIndex %= activePlayers.size
            startTurn(activePlayers[currentPlayerIndex])
        }
    }
}