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
    val SUIT_KEY = NamespacedKey(SaloonBetrayal.instance, "card_suit")
    val RANK_KEY = NamespacedKey(SaloonBetrayal.instance, "card_rank")

    fun GameCard.toItemStack(languageManager: LanguageManager, material: Material = Material.PAPER): ItemStack {
        val item = ItemStack(material)
        val meta: ItemMeta = item.itemMeta ?: return item

        meta.displayName(languageManager.getMessage(this.baseCard.namePath))

        val lore = mutableListOf<Component>()
        lore.add(languageManager.getMessage(this.baseCard.descriptionPath))
        meta.lore(lore)

        meta.persistentDataContainer.set(CARD_KEY, PersistentDataType.STRING, this.baseCard.id)
        meta.persistentDataContainer.set(SUIT_KEY, PersistentDataType.STRING, this.suit.name)
        meta.persistentDataContainer.set(RANK_KEY, PersistentDataType.STRING, this.rank.name)

        item.itemMeta = meta
        return item
    }

    fun ItemStack.getCardId(): String? {
        val meta = this.itemMeta ?: return null
        return meta.persistentDataContainer.get(CARD_KEY, PersistentDataType.STRING)
    }

    fun ItemStack.toGameCard(): GameCard? {
        val meta = this.itemMeta ?: return null
        val pdc = meta.persistentDataContainer

        val id = pdc.get(CARD_KEY, PersistentDataType.STRING) ?: return null
        val suitStr = pdc.get(SUIT_KEY, PersistentDataType.STRING) ?: return null
        val rankStr = pdc.get(RANK_KEY, PersistentDataType.STRING) ?: return null

        val baseCard = resolveBaseCard(id) ?: return null

        val suit = try { Suit.valueOf(suitStr) } catch (e: Exception) { return null }
        val rank = try { Rank.valueOf(rankStr) } catch (e: Exception) { return null }

        return GameCard(baseCard, suit, rank)
    }

    private fun resolveBaseCard(id: String): Card? {
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
            "volcanic" -> VolcanicCard
            "schofield" -> SchofieldCard
            "remington" -> RemingtonCard
            "rev_carabine" -> RevCarabineCard
            "winchester" -> WinchesterCard
            "mustang" -> MustangCard
            "scope" -> ScopeCard
            "barrel" -> BarrelCard
            "jail" -> JailCard
            "dynamite" -> DynamiteCard
            else -> null
        }
    }
}