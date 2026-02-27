package org.ReDiego0.saloonBetrayal.manager

import net.kyori.adventure.text.Component
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
}