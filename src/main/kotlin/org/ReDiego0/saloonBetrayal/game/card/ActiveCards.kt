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
        if (target == null) {
            player.sendMessage("§cDebes hacer clic derecho sobre un jugador para usar ¡BANG!.")
            return false
        }

        if (arena.turnManager.hasPlayedBangThisTurn && !arena.canPlayMultipleBangs(player)) {
            player.sendMessage("§cSolo puedes jugar una carta ¡BANG! por turno.")
            return false
        }

        if (!arena.canSee(player, target)) {
            player.sendMessage("§c¡El objetivo está fuera de tu alcance!")
            return false
        }

        arena.turnManager.registerBangPlayed()

        player.sendMessage("§eHas disparado a ${target.name}! Esperando su reacción...")
        target.sendMessage("§c¡${player.name} te ha disparado!")

        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
            arena = arena,
            victim = target,
            attackType = AttackType.BANG,
            attacker = player,
            onHit = {
                arena.takeDamage(target, 1)
                player.sendMessage("§a¡Tu disparo alcanzó a ${target.name}!")
            },
            onEvade = {
                player.sendMessage("§c¡${target.name} ha esquivado tu disparo!")
                target.sendMessage("§a¡Has esquivado el disparo de ${player.name}!")
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
        player.sendMessage("§cSolo puedes usar esta carta cuando te disparan (como reacción).")
        return false
    }
}

data object BeerCard : ActiveCard {
    override val id = "beer"
    override val namePath = "cards.beer.name"
    override val descriptionPath = "cards.beer.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        if (arena.players.size <= 2) {
            player.sendMessage("§c¡No puedes usar Cerveza cuando solo quedan 2 jugadores!")
            return false
        }

        val currentHealth = arena.getPlayerCurrentHealth(player)
        val maxHealth = arena.getPlayerMaxHealth(player)

        if (currentHealth >= maxHealth) {
            player.sendMessage("§cYa tienes la vida al máximo.")
            return false
        }

        player.health = ((currentHealth + 1) * 2).toDouble()
        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.displayManager.updateDisplay(player, arena)
        player.sendMessage("§a¡Te has curado 1 punto de vida bebiendo cerveza!")
        return true
    }
}

data object SaloonCard : ActiveCard {
    override val id = "saloon"
    override val namePath = "cards.saloon.name"
    override val descriptionPath = "cards.saloon.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        arena.players.forEach { p ->
            val currentHealth = arena.getPlayerCurrentHealth(p)
            val maxHealth = arena.getPlayerMaxHealth(p)

            if (currentHealth < maxHealth) {
                p.health = ((currentHealth + 1) * 2).toDouble()
                org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.displayManager.updateDisplay(p, arena)
                p.sendMessage("§a¡Alguien invitó una ronda en el Saloon! (+1 de vida)")
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
        val languageManager = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        repeat(2) {
            val drawnCard = arena.deck.draw()
            val itemStack = drawnCard.toItemStack(languageManager)
            player.inventory.addItem(itemStack)
        }
        player.sendMessage("§eHas usado Diligencia y robado 2 cartas.")
        return true
    }
}

data object WellsFargoCard : ActiveCard {
    override val id = "wells_fargo"
    override val namePath = "cards.wells_fargo.name"
    override val descriptionPath = "cards.wells_fargo.desc"

    override fun play(arena: Arena, player: Player, target: Player?): Boolean {
        val languageManager = org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.languageManager
        repeat(3) {
            val drawnCard = arena.deck.draw()
            val itemStack = drawnCard.toItemStack(languageManager)
            player.inventory.addItem(itemStack)
        }
        player.sendMessage("§eHas usado Wells Fargo y robado 3 cartas.")
        return true
    }
}

data object GeneralStoreCard : ActiveCard {
    override val id = "general_store"
    override val namePath = "cards.general_store.name"
    override val descriptionPath = "cards.general_store.desc"
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}

data object PanicCard : ActiveCard {
    override val id = "panic"
    override val namePath = "cards.panic.name"
    override val descriptionPath = "cards.panic.desc"
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}

data object CatBalouCard : ActiveCard {
    override val id = "cat_balou"
    override val namePath = "cards.cat_balou.name"
    override val descriptionPath = "cards.cat_balou.desc"
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}

data object DuelCard : ActiveCard {
    override val id = "duel"
    override val namePath = "cards.duel.name"
    override val descriptionPath = "cards.duel.desc"
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}

data object IndiansCard : ActiveCard {
    override val id = "indians"
    override val namePath = "cards.indians.name"
    override val descriptionPath = "cards.indians.desc"
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}

data object GatlingCard : ActiveCard {
    override val id = "gatling"
    override val namePath = "cards.gatling.name"
    override val descriptionPath = "cards.gatling.desc"
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}

data object JailCard : ActiveCard {
    override val id = "jail"
    override val namePath = "cards.jail.name"
    override val descriptionPath = "cards.jail.desc"
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}