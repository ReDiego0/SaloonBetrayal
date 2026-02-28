package org.ReDiego0.saloonBetrayal.listener

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.getCardId
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.toGameCard
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.toItemStack
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
        val reactionTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.reaction.title"))
        val generalStoreTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.general_store.title"))

        val discardBaseTitle = PlainTextComponentSerializer.plainText()
            .serialize(languageManager.getMessage("gui.discard.title", "amount" to "")).replace(" ", "")

        val isTargetCardMenu = org.ReDiego0.saloonBetrayal.game.card.PanicCard.pendingPanic.containsKey(player) ||
                org.ReDiego0.saloonBetrayal.game.card.CatBalouCard.pendingCatBalou.containsKey(player)

        when {
            plainTitle == equipmentTitle -> handleEquipmentMenu(event)
            plainTitle == confirmEndTitle -> handleConfirmEndMenu(event, player)
            plainTitle == drawCheckTitle -> handleDrawCheckMenu(event, player)
            plainTitle == reactionTitle -> handleReactionMenu(event, player)
            plainTitle == generalStoreTitle -> handleGeneralStoreMenu(event, player)
            isTargetCardMenu -> handleTargetCardMenu(event, player)
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
        val reactionTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.reaction.title"))
        val generalStoreTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.general_store.title"))

        val isTargetCardMenu = org.ReDiego0.saloonBetrayal.game.card.PanicCard.pendingPanic.containsKey(player) ||
                org.ReDiego0.saloonBetrayal.game.card.CatBalouCard.pendingCatBalou.containsKey(player)

        if (plainTitle == equipmentTitle || plainTitle == confirmEndTitle || plainTitle == drawCheckTitle || plainTitle == reactionTitle || plainTitle == generalStoreTitle || isTargetCardMenu) {
            val topInvSize = event.view.topInventory.size
            if (event.rawSlots.any { it < topInvSize }) {
                event.isCancelled = true
            }
        }
    }

    private fun handleReactionMenu(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true
        if (event.clickedInventory != event.view.topInventory) return
        SaloonBetrayal.instance.reactionManager.handleReactionClick(player, event.slot, event.currentItem)
    }

    private fun handleGeneralStoreMenu(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true
        if (event.clickedInventory != event.view.topInventory) return

        val clickedItem = event.currentItem ?: return
        val gameCard = clickedItem.toGameCard() ?: return

        val arena = arenaManager.getArena(player) ?: return
        val storeCards = org.ReDiego0.saloonBetrayal.game.card.GeneralStoreCard.currentStoreCards[arena.id] ?: return
        val queue = org.ReDiego0.saloonBetrayal.game.card.GeneralStoreCard.pickingQueue[arena.id] ?: return

        if (queue.firstOrNull() != player) return

        player.inventory.addItem(clickedItem.clone().apply { amount = 1 })
        val cardToRemove = storeCards.firstOrNull { it.baseCard.id == gameCard.baseCard.id && it.suit == gameCard.suit && it.rank == gameCard.rank }
        if (cardToRemove != null) storeCards.remove(cardToRemove)

        player.closeInventory()
        queue.removeAt(0)

        if (queue.isNotEmpty() && storeCards.isNotEmpty()) {
            val nextPlayer = queue.first()
            nextPlayer.sendMessage("§e¡Es tu turno de elegir una carta del Almacén!")
            SaloonBetrayal.instance.guiManager.openGeneralStoreMenu(nextPlayer, storeCards)
        } else {
            org.ReDiego0.saloonBetrayal.game.card.GeneralStoreCard.currentStoreCards.remove(arena.id)
            org.ReDiego0.saloonBetrayal.game.card.GeneralStoreCard.pickingQueue.remove(arena.id)
            storeCards.forEach { arena.deck.discard(it) }
        }
    }

    private fun handleTargetCardMenu(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true
        if (event.clickedInventory != event.view.topInventory) return

        val arena = arenaManager.getArena(player) ?: return
        val isPanic = org.ReDiego0.saloonBetrayal.game.card.PanicCard.pendingPanic.containsKey(player)

        val target = (if (isPanic) org.ReDiego0.saloonBetrayal.game.card.PanicCard.pendingPanic[player]
        else org.ReDiego0.saloonBetrayal.game.card.CatBalouCard.pendingCatBalou[player]) ?: return

        val clickedItem = event.currentItem ?: return

        if (event.slot == 22 && clickedItem.type == Material.PAPER) {
            val handCards = target.inventory.contents.filter { it != null && it.getCardId() != null }
            if (handCards.isNotEmpty()) {
                val randomCard = handCards.random()!!
                val gameCard = randomCard.toGameCard()

                randomCard.amount -= 1

                if (isPanic) {
                    player.inventory.addItem(randomCard.clone().apply { amount = 1 })
                    player.sendMessage("§aHas robado una carta al azar de la mano de ${target.name}.")
                } else {
                    if (gameCard != null) arena.deck.discard(gameCard)
                    player.sendMessage("§aHas descartado una carta al azar de la mano de ${target.name}.")
                }
            }
        }
        else {
            val gameCard = clickedItem.toGameCard() ?: return
            val targetEq = arena.playerEquipment[target] ?: return

            val cardToRemove = targetEq.firstOrNull { it.baseCard.id == gameCard.baseCard.id && it.suit == gameCard.suit && it.rank == gameCard.rank }
            if (cardToRemove != null) {
                targetEq.remove(cardToRemove)

                if (isPanic) {
                    val languageManager = SaloonBetrayal.instance.languageManager
                    player.inventory.addItem(cardToRemove.toItemStack(languageManager))
                    player.sendMessage("§aHas robado equipo de la mesa de ${target.name}.")
                } else {
                    arena.deck.discard(cardToRemove)
                    player.sendMessage("§aHas destruido equipo de la mesa de ${target.name}.")
                }
            }
        }
        if (isPanic) org.ReDiego0.saloonBetrayal.game.card.PanicCard.pendingPanic.remove(player)
        else org.ReDiego0.saloonBetrayal.game.card.CatBalouCard.pendingCatBalou.remove(player)

        player.closeInventory()
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

        val reactionTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.reaction.title"))
        val generalStoreTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.general_store.title"))
        val isTargetCardMenu = org.ReDiego0.saloonBetrayal.game.card.PanicCard.pendingPanic.containsKey(player) ||
                org.ReDiego0.saloonBetrayal.game.card.CatBalouCard.pendingCatBalou.containsKey(player)

        val isForcedAction = (plainTitle == reactionTitle && SaloonBetrayal.instance.reactionManager.pendingReactions.containsKey(player)) ||
                (plainTitle == generalStoreTitle && org.ReDiego0.saloonBetrayal.game.card.GeneralStoreCard.pickingQueue.values.any { it.firstOrNull() == player }) ||
                isTargetCardMenu

        if (isForcedAction) {
            val inventoryToReopen = event.inventory
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                SaloonBetrayal.instance,
                Runnable { player.openInventory(inventoryToReopen) },
                1L
            )
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