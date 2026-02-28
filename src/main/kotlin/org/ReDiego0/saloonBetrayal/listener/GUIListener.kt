package org.ReDiego0.saloonBetrayal.listener

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.getCardId
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.toGameCard
import org.ReDiego0.saloonBetrayal.game.card.PassiveCard
import org.ReDiego0.saloonBetrayal.game.card.GameCard
import org.ReDiego0.saloonBetrayal.manager.ArenaManager
import org.ReDiego0.saloonBetrayal.manager.LanguageManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

class GUIListener(
    private val arenaManager: ArenaManager,
    private val languageManager: LanguageManager
) : Listener {

    private val discardTracker = mutableMapOf<Player, Int>()

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val arena = arenaManager.getArena(player) ?: return
        val view = event.view
        val clickedInventory = event.clickedInventory ?: return

        val plainTitle = PlainTextComponentSerializer.plainText().serialize(view.title())
        val equipmentTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.equipment.title"))
        val confirmEndTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.confirm_end.title"))
        val drawCheckTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.draw_check.title"))

        val discardBaseTitle = PlainTextComponentSerializer.plainText()
            .serialize(languageManager.getMessage("gui.discard.title", "amount" to "")).replace(" ", "")

        when {
            plainTitle == equipmentTitle -> handleEquipmentMenu(event)
            plainTitle == confirmEndTitle -> handleConfirmEndMenu(event, player)
            plainTitle == drawCheckTitle -> handleDrawCheckMenu(event, player)
            plainTitle.replace(" ", "").contains(discardBaseTitle) -> handleDiscardMenu(event, player)
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        if (arenaManager.getArena(player) == null) return

        val plainTitle = PlainTextComponentSerializer.plainText().serialize(event.view.title())
        val equipmentTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.equipment.title"))
        val confirmEndTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.confirm_end.title"))
        val drawCheckTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.draw_check.title"))

        if (plainTitle == equipmentTitle || plainTitle == confirmEndTitle || plainTitle == drawCheckTitle) {
            val topInvSize = event.view.topInventory.size
            if (event.rawSlots.any { it < topInvSize }) {
                event.isCancelled = true
            }
        }
    }

    private fun handleEquipmentMenu(event: InventoryClickEvent) {
        if (event.clickedInventory == event.view.topInventory) {
            val slot = event.slot
            if (slot == 0 || slot == 1 || slot == 7 || slot == 8) {
                event.isCancelled = true
                return
            }
        }

        if (event.isShiftClick && event.clickedInventory == event.view.bottomInventory) {
            val item = event.currentItem
            val gameCard = item?.toGameCard()
            if (gameCard?.baseCard !is PassiveCard) {
                event.isCancelled = true
            }
        }

        if (event.clickedInventory == event.view.topInventory) {
            val cursorItem = event.cursor
            if (cursorItem != null && cursorItem.type != Material.AIR) {
                val gameCard = cursorItem.toGameCard()
                if (gameCard?.baseCard !is PassiveCard) {
                    event.isCancelled = true
                }
            }
        }
    }

    private fun handleConfirmEndMenu(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true
        if (event.clickedInventory != event.view.topInventory) return

        val arena = arenaManager.getArena(player) ?: return

        when (event.currentItem?.type) {
            Material.LIME_WOOL -> {
                player.closeInventory()

                val handCards = mutableListOf<ItemStack>()
                var totalCards = 0

                for (item in player.inventory.contents) {
                    if (item != null && item.getCardId() != null) {
                        handCards.add(item.clone())
                        totalCards += item.amount
                    }
                }

                val currentHealth = arena.getPlayerCurrentHealth(player)

                if (totalCards > currentHealth) {
                    val amountToDiscard = totalCards - currentHealth
                    discardTracker[player] = amountToDiscard

                    arena.turnManager.requestTurnEnd(player)

                    SaloonBetrayal.instance.guiManager.openDiscardMenu(
                        player,
                        amountToDiscard,
                        handCards
                    )
                } else {
                    arena.turnManager.passTurnToNext()
                }
            }

            Material.RED_WOOL -> player.closeInventory()
            else -> {}
        }
    }

    private fun handleDiscardMenu(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true
        if (event.clickedInventory != event.view.topInventory) return

        val clickedItem = event.currentItem ?: return

        val gameCard = clickedItem.toGameCard() ?: return

        val arena = arenaManager.getArena(player) ?: return
        var neededToDiscard = discardTracker[player] ?: return

        clickedItem.amount -= 1
        neededToDiscard -= 1
        discardTracker[player] = neededToDiscard

        arena.deck.discard(gameCard)

        if (neededToDiscard <= 0) {
            discardTracker.remove(player)
            player.inventory.clear()
            for (item in event.view.topInventory.contents) {
                if (item != null && item.toGameCard() != null) {
                    player.inventory.addItem(item)
                }
            }
            player.closeInventory()
            arena.turnManager.passTurnToNext()
        } else {
            player.sendMessage(languageManager.getMessage("messages.discard_remaining", "amount" to neededToDiscard.toString()))
        }
    }

    private fun handleDrawCheckMenu(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true
        if (event.clickedInventory != event.view.topInventory) return

        if (event.currentItem?.type == Material.PAPER) {
            SaloonBetrayal.instance.drawCheckManager.executeDraw(player)
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val plainTitle = PlainTextComponentSerializer.plainText().serialize(event.view.title())

        val equipmentTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.equipment.title"))
        if (plainTitle == equipmentTitle) {
            val arena = arenaManager.getArena(player) ?: return

            if (arena.state is org.ReDiego0.saloonBetrayal.game.GameState.Playing &&
                (arena.state as org.ReDiego0.saloonBetrayal.game.GameState.Playing).turnPhase == org.ReDiego0.saloonBetrayal.game.TurnPhase.Discard) {
                return
            }

            val equippedCards = mutableListOf<GameCard>()

            for (i in 2..6) {
                val item = event.inventory.getItem(i)
                val gameCard = item?.toGameCard()

                if (gameCard != null && gameCard.baseCard is PassiveCard) {
                    equippedCards.add(gameCard)
                } else if (item != null && item.type != Material.AIR) {
                    player.inventory.addItem(item)
                }
            }
            arena.playerEquipment[player] = equippedCards
            return
        }

        val amountNeeded = discardTracker[player] ?: return

        if (amountNeeded > 0) {
            val discardBaseTitle = PlainTextComponentSerializer.plainText()
                .serialize(languageManager.getMessage("gui.discard.title", "amount" to "")).replace(" ", "")

            if (plainTitle.replace(" ", "").contains(discardBaseTitle)) {
                player.sendMessage(languageManager.getMessage("messages.discard_cannot_close", "amount" to amountNeeded.toString()))

                val inventoryToReopen = event.inventory
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    SaloonBetrayal.instance,
                    Runnable {
                        player.openInventory(inventoryToReopen)
                    },
                    1L
                )
            }
        }
    }
}