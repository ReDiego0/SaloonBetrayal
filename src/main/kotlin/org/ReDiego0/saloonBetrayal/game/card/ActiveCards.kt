package org.ReDiego0.saloonBetrayal.game.card

import org.bukkit.entity.Player

data object BangCard : ActiveCard {
    override val id = "bang"
    override val namePath = "cards.bang.name"
    override val descriptionPath = "cards.bang.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object MissedCard : ActiveCard {
    override val id = "missed"
    override val namePath = "cards.missed.name"
    override val descriptionPath = "cards.missed.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object BeerCard : ActiveCard {
    override val id = "beer"
    override val namePath = "cards.beer.name"
    override val descriptionPath = "cards.beer.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object SaloonCard : ActiveCard {
    override val id = "saloon"
    override val namePath = "cards.saloon.name"
    override val descriptionPath = "cards.saloon.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object StagecoachCard : ActiveCard {
    override val id = "stagecoach"
    override val namePath = "cards.stagecoach.name"
    override val descriptionPath = "cards.stagecoach.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object WellsFargoCard : ActiveCard {
    override val id = "wells_fargo"
    override val namePath = "cards.wells_fargo.name"
    override val descriptionPath = "cards.wells_fargo.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object GeneralStoreCard : ActiveCard {
    override val id = "general_store"
    override val namePath = "cards.general_store.name"
    override val descriptionPath = "cards.general_store.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object PanicCard : ActiveCard {
    override val id = "panic"
    override val namePath = "cards.panic.name"
    override val descriptionPath = "cards.panic.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object CatBalouCard : ActiveCard {
    override val id = "cat_balou"
    override val namePath = "cards.cat_balou.name"
    override val descriptionPath = "cards.cat_balou.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object DuelCard : ActiveCard {
    override val id = "duel"
    override val namePath = "cards.duel.name"
    override val descriptionPath = "cards.duel.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object IndiansCard : ActiveCard {
    override val id = "indians"
    override val namePath = "cards.indians.name"
    override val descriptionPath = "cards.indians.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

data object GatlingCard : ActiveCard {
    override val id = "gatling"
    override val namePath = "cards.gatling.name"
    override val descriptionPath = "cards.gatling.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}

// En teoría es una pasiva, pero técnicamente es un ataque
data object JailCard : ActiveCard {
    override val id = "jail"
    override val namePath = "cards.jail.name"
    override val descriptionPath = "cards.jail.desc"
    override fun play(player: Player, target: Player?): Boolean = true
}