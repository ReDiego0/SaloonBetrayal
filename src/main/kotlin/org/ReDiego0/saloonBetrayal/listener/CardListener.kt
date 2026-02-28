package org.ReDiego0.saloonBetrayal.listener

import org.ReDiego0.saloonBetrayal.game.GameState
import org.ReDiego0.saloonBetrayal.game.TurnPhase
import org.ReDiego0.saloonBetrayal.game.card.ActiveCard
import org.ReDiego0.saloonBetrayal.game.card.BangCard
import org.ReDiego0.saloonBetrayal.game.card.BeerCard
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.getCardId
import org.ReDiego0.saloonBetrayal.game.card.CatBalouCard
import org.ReDiego0.saloonBetrayal.game.card.DuelCard
import org.ReDiego0.saloonBetrayal.game.card.GatlingCard
import org.ReDiego0.saloonBetrayal.game.card.GeneralStoreCard
import org.ReDiego0.saloonBetrayal.game.card.IndiansCard
import org.ReDiego0.saloonBetrayal.game.card.StagecoachCard
import org.ReDiego0.saloonBetrayal.game.card.JailCard // Importamos JailCard
import org.ReDiego0.saloonBetrayal.game.card.MissedCard
import org.ReDiego0.saloonBetrayal.game.card.PanicCard
import org.ReDiego0.saloonBetrayal.game.card.SaloonCard
import org.ReDiego0.saloonBetrayal.game.card.WellsFargoCard
import org.ReDiego0.saloonBetrayal.manager.ArenaManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class CardListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onCardUseSelf(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val player = event.player
        val item = event.item ?: return
        val cardId = item.getCardId() ?: return

        event.isCancelled = true

        val arena = arenaManager.getArena(player) ?: return
        val state = arena.state

        if (state !is GameState.Playing || state.currentPlayer != player || state.turnPhase != TurnPhase.Action) return

        val card = resolveCard(cardId) as? ActiveCard ?: return

        val success = card.play(arena, player)
        if (success) {
            item.amount -= 1
        }
    }

    @EventHandler
    fun onCardUseTarget(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val target = event.rightClicked as? Player ?: return

        val item = player.inventory.itemInMainHand
        val cardId = item.getCardId() ?: return

        event.isCancelled = true

        val arena = arenaManager.getArena(player) ?: return
        val state = arena.state

        if (state !is GameState.Playing || state.currentPlayer != player || state.turnPhase != TurnPhase.Action) return
        if (!arena.players.contains(target)) return

        val card = resolveCard(cardId) as? ActiveCard ?: return

        val success = card.play(arena, player, target)
        if (success) {
            item.amount -= 1
        }
    }

    private fun resolveCard(id: String): ActiveCard? {
        return when (id) {
            "bang" -> BangCard
            "missed" -> MissedCard
            "beer" -> BeerCard
            "saloon" -> SaloonCard
            "stagecoach" -> StagecoachCard
            "wells_fargo" -> WellsFargoCard
            "general_store" -> GeneralStoreCard
            "panic" -> PanicCard
            "cat_balou" -> CatBalouCard
            "duel" -> DuelCard
            "indians" -> IndiansCard
            "gatling" -> GatlingCard
            "jail" -> JailCard
            else -> null
        }
    }
}