package org.ReDiego0.saloonBetrayal.game

import org.bukkit.entity.Player

sealed class GameState {
    data object Waiting : GameState()
    data class Starting(val countdown: Int) : GameState()
    data class RoleSelection(val players: List<Player>) : GameState()
    data class Playing(val currentPlayer: Player, val turnPhase: TurnPhase) : GameState()
    data class Ended(val winners: List<Player>) : GameState()
}

sealed class TurnPhase {
    data object Draw : TurnPhase()
    data object Action : TurnPhase()
    data object Discard : TurnPhase()
}