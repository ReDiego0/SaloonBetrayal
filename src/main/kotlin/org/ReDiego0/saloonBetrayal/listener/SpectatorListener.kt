package org.ReDiego0.saloonBetrayal.listener

import org.ReDiego0.saloonBetrayal.manager.ArenaManager
import org.ReDiego0.saloonBetrayal.manager.LanguageManager
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

class SpectatorListener(private val arenaManager: ArenaManager, private val languageManager: LanguageManager) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val arena = arenaManager.getArena(player) ?: return
        if (arena.deadPlayers.contains(player) || arena.state is org.ReDiego0.saloonBetrayal.game.GameState.Playing) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val arena = arenaManager.getArena(player) ?: return

        if (arena.deadPlayers.contains(player) || arena.state is org.ReDiego0.saloonBetrayal.game.GameState.Playing) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val arena = arenaManager.getArena(player) ?: return

        if (arena.deadPlayers.contains(player)) {
            event.isCancelled = true

            if (event.item?.type == Material.RED_BED && (event.action.isRightClick || event.action.isLeftClick)) {
                arena.deadPlayers.remove(player)
                player.inventory.clear()
                player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
                player.gameMode = org.bukkit.GameMode.SURVIVAL

                // TODO: player.teleport(LobbyLocation)
                player.sendMessage(languageManager.getMessage("spectator.respawn"))
            }
        }
    }
}