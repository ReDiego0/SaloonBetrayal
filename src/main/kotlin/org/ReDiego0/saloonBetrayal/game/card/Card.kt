package org.ReDiego0.saloonBetrayal.game.card

import org.bukkit.entity.Player

sealed interface Card {
    val id: String
    val namePath: String
    val descriptionPath: String
}

sealed interface ActiveCard : Card {
    fun play(player: Player, target: Player? = null): Boolean
}

data object BangCard : ActiveCard {
    override val id = "bang"
    override val namePath = "cards.bang.name"
    override val descriptionPath = "cards.bang.desc"

    override fun play(player: Player, target: Player?): Boolean {
        if (target == null) return false

        return true
    }
}

data object BeerCard : ActiveCard {
    override val id = "beer"
    override val namePath = "cards.beer.name"
    override val descriptionPath = "cards.beer.desc"

    override fun play(player: Player, target: Player?): Boolean {
        return true
    }
}

data object StagecoachCard : ActiveCard {
    override val id = "stagecoach"
    override val namePath = "cards.stagecoach.name"
    override val descriptionPath = "cards.stagecoach.desc"

    override fun play(player: Player, target: Player?): Boolean {
        return true
    }
}