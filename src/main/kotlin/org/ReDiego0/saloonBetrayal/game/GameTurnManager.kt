package org.ReDiego0.saloonBetrayal.game

import org.ReDiego0.saloonBetrayal.game.card.Deck
import org.bukkit.entity.Player

class GameTurnManager(
    private val arena: Arena,
    private val deck: Deck
) {

    private var activePlayers = listOf<Player>()
    private var currentPlayerIndex = 0

    fun initialize(players: List<Player>) {
        activePlayers = players.toList()
        currentPlayerIndex = (0 until activePlayers.size).random()
        startTurn(activePlayers[currentPlayerIndex])
    }

    private fun startTurn(player: Player) {
        arena.updateState(GameState.Playing(player, TurnPhase.Draw))
        processDrawPhase(player)
    }

    private fun processDrawPhase(player: Player) {
        val card1 = deck.draw()
        val card2 = deck.draw()

        arena.updateState(GameState.Playing(player, TurnPhase.Action))
    }

    fun requestTurnEnd(player: Player): Boolean {
        val currentState = arena.state
        if (currentState !is GameState.Playing || currentState.currentPlayer != player) return false
        if (currentState.turnPhase != TurnPhase.Action) return false

        arena.updateState(GameState.Playing(player, TurnPhase.Discard))
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