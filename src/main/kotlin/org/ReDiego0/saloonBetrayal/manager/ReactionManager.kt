package org.ReDiego0.saloonBetrayal.manager

import org.ReDiego0.saloonBetrayal.game.Arena
import org.ReDiego0.saloonBetrayal.game.card.BarrelCard
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.getCardId
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class AttackType {
    BANG,
    INDIANS,
    DUEL
}

class ReactionManager(
    private val languageManager: LanguageManager,
    private val drawCheckManager: DrawCheckManager
) {
    data class PendingReaction(
        val arena: Arena,
        val attackType: AttackType,
        val attacker: Player?,
        var barrelUsed: Boolean = false,
        val onHit: () -> Unit,
        val onEvade: () -> Unit
    )

    val pendingReactions = mutableMapOf<Player, PendingReaction>()

    fun requestReaction(
        arena: Arena,
        victim: Player,
        attackType: AttackType,
        attacker: Player?,
        onHit: () -> Unit,
        onEvade: () -> Unit
    ) {
        pendingReactions[victim] = PendingReaction(arena, attackType, attacker, false, onHit, onEvade)
        openReactionMenu(victim)
    }

    fun openReactionMenu(victim: Player) {
        val pending = pendingReactions[victim] ?: return
        val arena = pending.arena

        val title = languageManager.getMessage("gui.reaction.title")
        val inventory = Bukkit.createInventory(null, 27, title)

        val takeHitItem = ItemStack(Material.RED_WOOL)
        val takeHitMeta = takeHitItem.itemMeta
        takeHitMeta?.displayName(languageManager.getMessage("gui.reaction.take_hit"))
        takeHitItem.itemMeta = takeHitMeta
        inventory.setItem(15, takeHitItem)

        val requiredCardId = when (pending.attackType) {
            AttackType.BANG -> "missed"
            AttackType.INDIANS, AttackType.DUEL -> "bang"
        }

        val hasRequiredCard = victim.inventory.contents.any { it != null && it.getCardId() == requiredCardId }

        if (hasRequiredCard) {
            val playCardItem = ItemStack(Material.PAPER)
            val playCardMeta = playCardItem.itemMeta
            playCardMeta?.displayName(languageManager.getMessage("gui.reaction.play_card", "card" to requiredCardId))
            playCardItem.itemMeta = playCardMeta
            inventory.setItem(11, playCardItem)
        }

        if (pending.attackType == AttackType.BANG && !pending.barrelUsed) {
            val equipment = arena.playerEquipment[victim] ?: emptyList()
            val hasBarrel = equipment.any { it.baseCard is BarrelCard }

            if (hasBarrel) {
                val barrelItem = ItemStack(Material.BARREL)
                val barrelMeta = barrelItem.itemMeta
                barrelMeta?.displayName(languageManager.getMessage("gui.reaction.use_barrel"))
                barrelItem.itemMeta = barrelMeta
                inventory.setItem(13, barrelItem)
            }
        }

        victim.openInventory(inventory)
    }

    fun handleReactionClick(player: Player, clickedSlot: Int, clickedItem: ItemStack?) {
        val pending = pendingReactions[player] ?: return
        val arena = pending.arena

        when (clickedItem?.type) {
            Material.RED_WOOL -> {
                pendingReactions.remove(player)
                player.closeInventory()
                pending.onHit()
            }

            Material.PAPER -> {
                val requiredCardId = when (pending.attackType) {
                    AttackType.BANG -> "missed"
                    AttackType.INDIANS, AttackType.DUEL -> "bang"
                }

                val cardToRemove = player.inventory.contents.firstOrNull { it != null && it.getCardId() == requiredCardId }

                if (cardToRemove != null) {
                    val gameCard = org.ReDiego0.saloonBetrayal.game.card.CardMapper.run { cardToRemove.toGameCard() }
                    cardToRemove.amount -= 1

                    if (gameCard != null) {
                        arena.deck.discard(gameCard)
                    }

                    pendingReactions.remove(player)
                    player.closeInventory()
                    pending.onEvade()
                } else {
                    player.sendMessage(languageManager.getMessage("messages.error_card_not_found"))
                }
            }

            Material.BARREL -> {
                pending.barrelUsed = true
                player.closeInventory()
                drawCheckManager.requestDrawCheck(
                    player = player,
                    arena = arena,
                    reasonPath = "reasons.barrel",
                    condition = { suit, _ -> suit == org.ReDiego0.saloonBetrayal.game.card.Suit.HEARTS }
                ) { isSuccess ->
                    if (isSuccess) {
                        pendingReactions.remove(player)
                        pending.onEvade()
                    } else {
                        openReactionMenu(player)
                    }
                }
            }
            else -> {}
        }
    }
}