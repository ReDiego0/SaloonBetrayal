package org.ReDiego0.saloonBetrayal.listener

import org.ReDiego0.saloonBetrayal.game.GameState
import org.ReDiego0.saloonBetrayal.game.TurnPhase
import org.ReDiego0.saloonBetrayal.manager.ArenaManager
import org.ReDiego0.saloonBetrayal.manager.GUIManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class HotkeyListener(
    private val arenaManager: ArenaManager,
    private val guiManager: GUIManager
) : Listener {

    @EventHandler
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        val arena = arenaManager.getArena(player) ?: return

        event.isCancelled = true

        guiManager.openEquipmentMenu(player)
    }

    @EventHandler
    fun onDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        val arena = arenaManager.getArena(player) ?: return

        event.isCancelled = true

        val state = arena.state
        if (state is GameState.Playing && state.currentPlayer == player && state.turnPhase == TurnPhase.Action) {
            guiManager.openEndTurnConfirmMenu(player)
        }
    }
}