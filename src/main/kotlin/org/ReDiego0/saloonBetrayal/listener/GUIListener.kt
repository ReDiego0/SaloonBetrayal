package org.ReDiego0.saloonBetrayal.listener

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.ReDiego0.saloonBetrayal.manager.ArenaManager
import org.ReDiego0.saloonBetrayal.manager.LanguageManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class GUIListener(
    private val arenaManager: ArenaManager,
    private val languageManager: LanguageManager
) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val arena = arenaManager.getArena(player) ?: return
        val view = event.view
        val clickedInventory = event.clickedInventory ?: return

        val plainTitle = PlainTextComponentSerializer.plainText().serialize(view.title())
        val equipmentTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.equipment.title"))
        val confirmEndTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.confirm_end.title"))

        when (plainTitle) {
            equipmentTitle -> handleEquipmentMenu(event)
            confirmEndTitle -> handleConfirmEndMenu(event, player)
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        if (arenaManager.getArena(player) == null) return

        val plainTitle = PlainTextComponentSerializer.plainText().serialize(event.view.title())
        val equipmentTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.equipment.title"))
        val confirmEndTitle = PlainTextComponentSerializer.plainText().serialize(languageManager.getMessage("gui.confirm_end.title"))

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

        when (event.currentItem?.type) {
            Material.LIME_WOOL -> {
                player.closeInventory()
                // TODO: Calcular (Cartas en mano) vs (Vida máxima actual)
                // Si Cartas > Vida -> Abrir Menú de Descarte
                // Si no -> Terminar el turno
            }
            Material.RED_WOOL -> {
                player.closeInventory()
            }
            else -> {}
        }
    }
}