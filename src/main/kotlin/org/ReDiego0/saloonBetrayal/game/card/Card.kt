package org.ReDiego0.saloonBetrayal.game.card

import org.bukkit.entity.Player

enum class Suit(val namePath: String) {
    SPADES("suits.spades"),
    HEARTS("suits.hearts"),
    CLUBS("suits.clubs"),
    DIAMONDS("suits.diamonds")
}

enum class Rank(val symbol: String, val numericValue: Int) {
    TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5),
    SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9),
    TEN("10", 10), JACK("J", 11), QUEEN("Q", 12), KING("K", 13), ACE("A", 14)
}

data class GameCard(
    val baseCard: Card,
    val suit: Suit,
    val rank: Rank
)

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