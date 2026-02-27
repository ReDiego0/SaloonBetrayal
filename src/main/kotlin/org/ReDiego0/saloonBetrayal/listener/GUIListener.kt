package org.ReDiego0.saloonBetrayal.listener

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.getCardId
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
        val equipmentTitle =
            PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.equipment.title"))
        val confirmEndTitle =
            PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.confirm_end.title"))

        val discardBaseTitle = PlainTextComponentSerializer.plainText()
            .serialize(languageManager.getMessage("gui.discard.title", "amount" to "")).replace(" ", "")

        when {
            plainTitle == equipmentTitle -> handleEquipmentMenu(event)
            plainTitle == confirmEndTitle -> handleConfirmEndMenu(event, player)
            plainTitle.replace(" ", "").contains(discardBaseTitle) -> handleDiscardMenu(event, player)
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        if (arenaManager.getArena(player) == null) return

        val plainTitle = PlainTextComponentSerializer.plainText().serialize(event.view.title())
        val equipmentTitle =
            PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.equipment.title"))
        val confirmEndTitle =
            PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.confirm_end.title"))

        if (plainTitle == equipmentTitle || plainTitle == confirmEndTitle) {
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

                val maxHealth = arena.getPlayerMaxHealth(player)

                if (totalCards > maxHealth) {
                    val amountToDiscard = totalCards - maxHealth
                    discardTracker[player] = amountToDiscard
                    org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.guiManager.openDiscardMenu(
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
        if (clickedItem.getCardId() == null) return

        val arena = arenaManager.getArena(player) ?: return
        var neededToDiscard = discardTracker[player] ?: return

        clickedItem.amount -= 1
        neededToDiscard -= 1
        discardTracker[player] = neededToDiscard

        if (neededToDiscard <= 0) {
            discardTracker.remove(player)

            player.inventory.clear()
            for (item in event.view.topInventory.contents) {
                if (item != null && item.getCardId() != null) {
                    player.inventory.addItem(item)
                }
            }

            player.closeInventory()
            arena.turnManager.passTurnToNext()
        } else {
            player.sendMessage(languageManager.getMessage("messages.discard_remaining", "amount" to neededToDiscard.toString()))
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val amountNeeded = discardTracker[player] ?: return

        if (amountNeeded > 0) {
            val plainTitle = PlainTextComponentSerializer.plainText().serialize(event.view.title())
            val discardBaseTitle = PlainTextComponentSerializer.plainText()
                .serialize(languageManager.getMessage("gui.discard.title", "amount" to "")).replace(" ", "")

            if (plainTitle.replace(" ", "").contains(discardBaseTitle)) {
                player.sendMessage("¡No puedes cerrar el inventario! Aún debes descartar $amountNeeded cartas.")

                val inventoryToReopen = event.inventory
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance,
                    Runnable {
                        player.openInventory(inventoryToReopen)
                    },
                    1L
                )
            }
        }
    }
}