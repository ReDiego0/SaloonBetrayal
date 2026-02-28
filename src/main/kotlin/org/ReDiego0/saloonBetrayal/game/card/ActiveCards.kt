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
        if (target == null) return false

        if (arena.getDistance(player, target) > 1) {
            player.sendMessage("§cEl objetivo está demasiado lejos. Pánico solo tiene alcance 1.")
            return false
        }

        val targetEq = arena.playerEquipment[target] ?: emptyList()
        val hasHandCards = target.inventory.contents.any { it != null && CardMapper.run { it.getCardId() } != null }

        if (targetEq.isEmpty() && !hasHandCards) {
            player.sendMessage("§cEl objetivo no tiene cartas para robar.")
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
        if (target == null) return false
        val targetEq = arena.playerEquipment[target] ?: emptyList()
        val hasHandCards = target.inventory.contents.any { it != null && CardMapper.run { it.getCardId() } != null }

        if (targetEq.isEmpty() && !hasHandCards) {
            player.sendMessage("§cEl objetivo no tiene cartas para descartar.")
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
        if (target == null) return false

        player.sendMessage("§e¡Has desafiado a ${target.name} a un Duelo!")
        target.sendMessage("§c¡${player.name} te ha desafiado a un Duelo!")

        startDuelPingPong(arena, target, player)
        return true
    }

    private fun startDuelPingPong(arena: Arena, currentDefender: Player, currentAttacker: Player) {
        org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
            arena = arena,
            victim = currentDefender,
            attackType = AttackType.DUEL,
            attacker = currentAttacker,
            onHit = {
                arena.takeDamage(currentDefender, 1)
                currentAttacker.sendMessage("§a¡Has ganado el Duelo contra ${currentDefender.name}!")
            },
            onEvade = {
                currentDefender.sendMessage("§e¡Has devuelto el fuego en el Duelo!")
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
        player.sendMessage("§e¡Has enviado a los Indios a atacar a todos!")

        // Filtramos a todos los vivos menos al que jugó la carta
        val targets = arena.players.filter { it != player }

        for (victim in targets) {
            victim.sendMessage("§c¡${player.name} ha llamado a los Indios! ¡Defiéndete con un ¡BANG!!")

            org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
                arena = arena,
                victim = victim,
                attackType = org.ReDiego0.saloonBetrayal.manager.AttackType.INDIANS, // Exige BANG! y no deja usar Barril
                attacker = player,
                onHit = {
                    arena.takeDamage(victim, 1)
                    player.sendMessage("§a¡Los Indios hirieron a ${victim.name}!")
                },
                onEvade = {
                    player.sendMessage("§c¡${victim.name} logró repeler a los Indios!")
                    victim.sendMessage("§a¡Te has defendido de los Indios de ${player.name} con éxito!")
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
        player.sendMessage("§e¡Has disparado la Gatling contra todos!")

        val targets = arena.players.filter { it != player }

        for (victim in targets) {
            victim.sendMessage("§c¡${player.name} está disparando una Gatling! ¡Cúbrete!")

            org.ReDiego0.saloonBetrayal.SaloonBetrayal.instance.reactionManager.requestReaction(
                arena = arena,
                victim = victim,
                attackType = AttackType.BANG,
                attacker = player,
                onHit = {
                    arena.takeDamage(victim, 1)
                    player.sendMessage("§a¡Tu Gatling alcanzó a ${victim.name}!")
                },
                onEvade = {
                    player.sendMessage("§c¡${victim.name} se cubrió de tu Gatling!")
                    victim.sendMessage("§a¡Has esquivado la ráfaga de Gatling de ${player.name}!")
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
    override fun play(arena: Arena, player: Player, target: Player?): Boolean = true
}