package org.ReDiego0.saloonBetrayal.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.toItemStack
import org.ReDiego0.saloonBetrayal.game.card.GameCard
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GUIManager(private val languageManager: LanguageManager) {

    fun openEquipmentMenu(player: Player) {
        val title = languageManager.getMessage("gui.equipment.title")
        val inventory = Bukkit.createInventory(null, 9, title)

        val separator = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val meta = separator.itemMeta
        meta?.displayName(Component.empty())
        separator.itemMeta = meta

        inventory.setItem(0, separator)
        inventory.setItem(1, separator)
        inventory.setItem(7, separator)
        inventory.setItem(8, separator)

        player.openInventory(inventory)
    }

    fun openEndTurnConfirmMenu(player: Player) {
        val title = languageManager.getMessage("gui.confirm_end.title")
        val inventory = Bukkit.createInventory(null, 27, title)

        val confirmItem = ItemStack(Material.LIME_WOOL)
        val confirmMeta = confirmItem.itemMeta
        confirmMeta?.displayName(languageManager.getMessage("gui.confirm_end.yes"))
        confirmItem.itemMeta = confirmMeta

        val cancelItem = ItemStack(Material.RED_WOOL)
        val cancelMeta = cancelItem.itemMeta
        cancelMeta?.displayName(languageManager.getMessage("gui.confirm_end.no"))
        cancelItem.itemMeta = cancelMeta

        inventory.setItem(11, confirmItem)
        inventory.setItem(15, cancelItem)

        player.openInventory(inventory)
    }

    fun openDiscardMenu(player: Player, amountToDiscard: Int, handCards: List<ItemStack>) {
        val titleMsg = languageManager.getMessage("gui.discard.title", "amount" to amountToDiscard.toString())
        val inventory = Bukkit.createInventory(null, 54, titleMsg)

        handCards.forEach { inventory.addItem(it) }
        player.inventory.clear()

        player.openInventory(inventory)
    }

    fun openTargetCardMenu(attacker: Player, targetName: String, equipment: List<GameCard>, hasHandCards: Boolean, actionMsgPath: String) {
        val actionText = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage(actionMsgPath))
        val titleComponent = languageManager.getMessage("gui.target_cards.title", "action" to actionText, "target" to targetName)

        val inventory = Bukkit.createInventory(null, 27, titleComponent)

        equipment.forEachIndexed { index, card ->
            if (index < 9) {
                inventory.setItem(index, card.toItemStack(languageManager))
            }
        }

        if (hasHandCards) {
            val handItem = ItemStack(Material.PAPER)
            val meta = handItem.itemMeta
            meta?.displayName(languageManager.getMessage("gui.target_cards.hand_cards"))
            handItem.itemMeta = meta
            inventory.setItem(22, handItem)
        }

        attacker.openInventory(inventory)
    }

    fun openGeneralStoreMenu(player: Player, storeCards: List<GameCard>) {
        val title = languageManager.getMessage("gui.general_store.title")
        val inventory = Bukkit.createInventory(null, 9, title)

        storeCards.forEach { card ->
            inventory.addItem(card.toItemStack(languageManager))
        }

        player.openInventory(inventory)
    }
}