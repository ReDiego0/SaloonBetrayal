package org.ReDiego0.saloonBetrayal.game.card

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.ReDiego0.saloonBetrayal.manager.LanguageManager

object CardMapper {

    val CARD_KEY = NamespacedKey(SaloonBetrayal.instance, "card_id")

    fun Card.toItemStack(languageManager: LanguageManager, material: Material = Material.PAPER): ItemStack {
        val item = ItemStack(material)
        val meta: ItemMeta = item.itemMeta ?: return item

        meta.displayName(languageManager.getMessage(this.namePath))

        val lore = mutableListOf<Component>()
        lore.add(languageManager.getMessage(this.descriptionPath))
        meta.lore(lore)

        meta.persistentDataContainer.set(CARD_KEY, PersistentDataType.STRING, this.id)

        item.itemMeta = meta
        return item
    }

    fun ItemStack.getCardId(): String? {
        val meta = this.itemMeta ?: return null
        return meta.persistentDataContainer.get(CARD_KEY, PersistentDataType.STRING)
    }
}