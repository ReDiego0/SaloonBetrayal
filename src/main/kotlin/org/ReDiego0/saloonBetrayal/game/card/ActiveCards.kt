package org.ReDiego0.saloonBetrayal.game.card

import org.ReDiego0.saloonBetrayal.game.Arena
import org.ReDiego0.saloonBetrayal.game.card.CardMapper.toItemStack
import org.ReDiego0.saloonBetrayal.manager.AttackType
import org.bukkit.entity.Player

data object BangCard : ActiveCard {
    override val id = "bang"
    override val namePath = "cards.bang.name"
    override val descriptionPath = "cards.bang.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        if (target == null) {
            player.sendMessage(lm.getMessage("messages.error_target_required"))
            return false
        }

        if (arena.turnManager.hasPlayedBangThisTurn && !arena.canPlayMultipleBangs(player)) {
            player.sendMessage(lm.getMessage("messages.error_bang_limit"))
            return false
        }

        if (!arena.canSee(player, target)) {
            player.sendMessage(lm.getMessage("messages.error_out_of_range"))
            return false
        }

        arena.turnManager.registerBangPlayed()

        player.sendMessage(lm.getMessage("messages.bang_fired_self", "target" to target.name))
        target.sendMessage(lm.getMessage("messages.bang_fired_target", "player" to player.name))

        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
            arena = arena,
            victim = target,
            attackType = AttackType.BANG,
            attacker = player,
            onHit = {
                arena.takeDamage(target, 1)
                player.sendMessage(lm.getMessage("messages.bang_hit_self", "target" to target.name))
            },
            onEvade = {
                player.sendMessage(lm.getMessage("messages.bang_evaded_self", "target" to target.name))
                target.sendMessage(lm.getMessage("messages.bang_evaded_target", "player" to player.name))
            }
        )

        return true
    }
}

data object MissedCard : ActiveCard {
    override val id = "missed"
    override val namePath = "cards.missed.name"
    override val descriptionPath = "cards.missed.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        player.sendMessage(lm.getMessage("messages.error_missed_usage"))
        return false
    }
}

data object BeerCard : ActiveCard {
    override val id = "beer"
    override val namePath = "cards.beer.name"
    override val descriptionPath = "cards.beer.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        if (arena.players.size <= 2) {
            player.sendMessage(lm.getMessage("messages.error_beer_two_players"))
            return false
        }

        val currentHealth = arena.getPlayerCurrentHealth(player)
        val maxHealth = arena.getPlayerMaxHealth(player)

        if (currentHealth >= maxHealth) {
            player.sendMessage(lm.getMessage("messages.error_max_health"))
            return false
        }

        player.health = ((currentHealth + 1) * 2).toDouble()
        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.displayManager.updateDisplay(player, arena)
        player.sendMessage(lm.getMessage("messages.beer_healed"))
        return true
    }
}

data object SaloonCard : ActiveCard {
    override val id = "saloon"
    override val namePath = "cards.saloon.name"
    override val descriptionPath = "cards.saloon.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        arena.players.forEach { p ->
            val currentHealth = arena.getPlayerCurrentHealth(p)
            val maxHealth = arena.getPlayerMaxHealth(p)

            if (currentHealth < maxHealth) {
                p.health = ((currentHealth + 1) * 2).toDouble()
                org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.displayManager.updateDisplay(p, arena)
                p.sendMessage(lm.getMessage("messages.saloon_healed"))
            }
        }
        return true
    }
}

data object StagecoachCard : ActiveCard {
    override val id = "stagecoach"
    override val namePath = "cards.stagecoach.name"
    override val descriptionPath = "cards.stagecoach.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        repeat(2) {
            val drawnCard = arena.deck.draw()
            val itemStack = drawnCard.toItemStack(lm)
            player.inventory.addItem(itemStack)
        }
        player.sendMessage(lm.getMessage("messages.stagecoach_used"))
        return true
    }
}

data object WellsFargoCard : ActiveCard {
    override val id = "wells_fargo"
    override val namePath = "cards.wells_fargo.name"
    override val descriptionPath = "cards.wells_fargo.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        repeat(3) {
            val drawnCard = arena.deck.draw()
            val itemStack = drawnCard.toItemStack(lm)
            player.inventory.addItem(itemStack)
        }
        player.sendMessage(lm.getMessage("messages.wellsfargo_used"))
        return true
    }
}

data object GeneralStoreCard : ActiveCard {
    override val id = "general_store"
    override val namePath = "cards.general_store.name"
    override val descriptionPath = "cards.general_store.desc"

    val currentStoreCards = mutableMapOf<String, MutableList<GameCard>>()
    var pickingQueue = mutableMapOf<String, MutableList<Player>>()

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val numPlayers = arena.players.size
        val drawnCards = mutableListOf<GameCard>()

        repeat(numPlayers) { drawnCards.add(arena.deck.draw()) }

        currentStoreCards[arena.id] = drawnCards

        val playerIndex = arena.players.indexOf(player)
        val queue = mutableListOf<Player>()
        for (i in 0 until numPlayers) {
            queue.add(arena.players[(playerIndex + i) % numPlayers])
        }
        pickingQueue[arena.id] = queue

        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.guiManager.openGeneralStoreMenu(queue.first(), drawnCards)
        return true
    }
}

data object PanicCard : ActiveCard {
    override val id = "panic"
    override val namePath = "cards.panic.name"
    override val descriptionPath = "cards.panic.desc"

    val pendingPanic = mutableMapOf<Player, Player>()

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        if (target == null) return false

        if (arena.getDistance(player, target) > 1) {
            player.sendMessage(lm.getMessage("messages.error_panic_range"))
            return false
        }

        val targetEq = arena.playerEquipment[target] ?: emptyList()
        val hasHandCards = target.inventory.contents.any { it != null && CardMapper.run { it.getCardId() } != null }

        if (targetEq.isEmpty() && !hasHandCards) {
            player.sendMessage(lm.getMessage("messages.error_no_cards_to_steal"))
            return false
        }

        pendingPanic[player] = target
        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.guiManager.openTargetCardMenu(player, target.name, targetEq, hasHandCards, "gui.actions.steal")
        return true
    }
}

data object CatBalouCard : ActiveCard {
    override val id = "cat_balou"
    override val namePath = "cards.cat_balou.name"
    override val descriptionPath = "cards.cat_balou.desc"

    val pendingCatBalou = mutableMapOf<Player, Player>()

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        if (target == null) return false

        val targetEq = arena.playerEquipment[target] ?: emptyList()
        val hasHandCards = target.inventory.contents.any { it != null && CardMapper.run { it.getCardId() } != null }

        if (targetEq.isEmpty() && !hasHandCards) {
            player.sendMessage(lm.getMessage("messages.error_no_cards_to_discard"))
            return false
        }

        pendingCatBalou[player] = target
        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.guiManager.openTargetCardMenu(player, target.name, targetEq, hasHandCards, "gui.actions.discard")
        return true
    }
}

data object DuelCard : ActiveCard {
    override val id = "duel"
    override val namePath = "cards.duel.name"
    override val descriptionPath = "cards.duel.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        if (target == null) return false

        player.sendMessage(lm.getMessage("messages.duel_challenged_self", "target" to target.name))
        target.sendMessage(lm.getMessage("messages.duel_challenged_target", "player" to player.name))

        startDuelPingPong(arena, target, player)
        return true
    }

    private fun startDuelPingPong(arena: Arena, currentDefender: Player, currentAttacker: Player) {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
            arena = arena,
            victim = currentDefender,
            attackType = AttackType.DUEL,
            attacker = currentAttacker,
            onHit = {
                arena.takeDamage(currentDefender, 1)
                currentAttacker.sendMessage(lm.getMessage("messages.duel_won", "target" to currentDefender.name))
            },
            onEvade = {
                currentDefender.sendMessage(lm.getMessage("messages.duel_returned_fire"))
                startDuelPingPong(arena, currentAttacker, currentDefender)
            }
        )
    }
}

data object IndiansCard : ActiveCard {
    override val id = "indians"
    override val namePath = "cards.indians.name"
    override val descriptionPath = "cards.indians.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        player.sendMessage(lm.getMessage("messages.indians_sent"))

        val targets = arena.players.filter { it != player }

        for (victim in targets) {
            victim.sendMessage(lm.getMessage("messages.indians_attacked", "player" to player.name))

            org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
                arena = arena,
                victim = victim,
                attackType = org.ReDiego0.saloonBetrayal.manager.AttackType.INDIANS,
                attacker = player,
                onHit = {
                    arena.takeDamage(victim, 1)
                    player.sendMessage(lm.getMessage("messages.indians_hit_other", "target" to victim.name))
                },
                onEvade = {
                    player.sendMessage(lm.getMessage("messages.indians_evaded_other", "target" to victim.name))
                    victim.sendMessage(lm.getMessage("messages.indians_evaded_self", "player" to player.name))
                }
            )
        }
        return true
    }
}

data object GatlingCard : ActiveCard {
    override val id = "gatling"
    override val namePath = "cards.gatling.name"
    override val descriptionPath = "cards.gatling.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        player.sendMessage(lm.getMessage("messages.gatling_fired"))

        val targets = arena.players.filter { it != player }

        for (victim in targets) {
            victim.sendMessage(lm.getMessage("messages.gatling_attacked", "player" to player.name))

            org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
                arena = arena,
                victim = victim,
                attackType = AttackType.BANG,
                attacker = player,
                onHit = {
                    arena.takeDamage(victim, 1)
                    player.sendMessage(lm.getMessage("messages.gatling_hit_other", "target" to victim.name))
                },
                onEvade = {
                    player.sendMessage(lm.getMessage("messages.gatling_evaded_other", "target" to victim.name))
                    victim.sendMessage(lm.getMessage("messages.gatling_evaded_self", "player" to player.name))
                }
            )
        }
        return true
    }
}

data object JailCard : ActiveCard {
    override val id = "jail"
    override val namePath = "cards.jail.name"
    override val descriptionPath = "cards.jail.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val lm = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        if (target == null) return false

        if (player == target) {
            player.sendMessage(lm.getMessage("messages.error_jail_self"))
            return false
        }

        if (arena.playerRoles[target] == org.ReDiego0.saloonBetrayal.game.role.Role.Sheriff) {
            player.sendMessage(lm.getMessage("messages.error_jail_sheriff"))
            return false
        }

        val targetEquipment = arena.playerEquipment[target] ?: return false

        if (targetEquipment.any { it.baseCard.id == "jail" }) {
            player.sendMessage(lm.getMessage("messages.error_jail_already", "target" to target.name))
            return false
        }

        val itemInHand = player.inventory.itemInMainHand
        val gameCard = CardMapper.run { itemInHand.toGameCard() }

        if (gameCard != null) {
            targetEquipment.add(gameCard)
            player.sendMessage(lm.getMessage("messages.jail_imprisoned_self", "target" to target.name))
            target.sendMessage(lm.getMessage("messages.jail_imprisoned_target", "player" to player.name))
            return true
        }

        return false
    }
}