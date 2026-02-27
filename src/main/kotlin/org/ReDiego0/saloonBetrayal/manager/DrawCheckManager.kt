package org.ReDiego0.saloonBetrayal.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.ReDiego0.saloonBetrayal.game.Arena
import org.ReDiego0.saloonBetrayal.game.card.Suit
import org.ReDiego0.saloonBetrayal.game.card.Rank
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DrawCheckManager(
    private val languageManager: LanguageManager
) {

    private data class PendingDraw(
        val arena: Arena,
        val reasonPath: String,
        val condition: (Suit, Rank) -> Boolean,
        val onComplete: (Boolean) -> Unit
    )

    private val pendingDraws = mutableMapOf<Player, PendingDraw>()

    fun requestDrawCheck(
        player: Player,
        arena: Arena,
        reasonPath: String,
        condition: (Suit, Rank) -> Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        pendingDraws[player] = PendingDraw(arena, reasonPath, condition, onComplete)
        openDrawMenu(player)
    }

    private fun openDrawMenu(player: Player) {
        val title = languageManager.getMessage("gui.draw_check.title")
        val inventory = Bukkit.createInventory(null, 9, title)

        val drawButton = ItemStack(Material.PAPER)
        val meta = drawButton.itemMeta
        meta?.displayName(languageManager.getMessage("gui.draw_check.button"))
        drawButton.itemMeta = meta

        inventory.setItem(4, drawButton)
        player.openInventory(inventory)
    }

    fun executeDraw(player: Player): Boolean {
        val pending = pendingDraws.remove(player) ?: return false
        val arena = pending.arena
        val drawnCard = arena.deck.draw()
        val isSuccess = pending.condition(drawnCard.suit, drawnCard.rank)

        val reasonMsg = languageManager.getMessage(pending.reasonPath)
        val cardName = languageManager.getMessage(drawnCard.baseCard.namePath)
        val cardDesc = languageManager.getMessage(drawnCard.baseCard.descriptionPath)
        val suitName = languageManager.getMessage(drawnCard.suit.namePath)
        val rankSymbol = drawnCard.rank.symbol

        val hoverableCardMsg = languageManager.getMessage(
            "messages.draw_hover_format",
            "name" to PlainTextComponentSerializer.plainText().serialize(cardName),
            "suit" to PlainTextComponentSerializer.plainText().serialize(suitName),
            "rank" to rankSymbol,
            "desc" to PlainTextComponentSerializer.plainText().serialize(cardDesc)
        )

        val broadcastMsg = languageManager.getMessage(
            "messages.draw_broadcast",
            "player" to player.name,
            "reason" to PlainTextComponentSerializer.plainText().serialize(reasonMsg),
            "card_hover" to PlainTextComponentSerializer.plainText().serialize(hoverableCardMsg)
        )

        val resultMsg = if (isSuccess) {
            languageManager.getMessage("messages.draw_success")
        } else {
            languageManager.getMessage("messages.draw_fail")
        }

        arena.players.plus(arena.deadPlayers).forEach { p ->
            p.sendMessage(broadcastMsg)
            p.sendMessage(resultMsg)
        }

        arena.deck.discard(drawnCard)

        player.closeInventory()

        pending.onComplete(isSuccess)

        return true
    }
}