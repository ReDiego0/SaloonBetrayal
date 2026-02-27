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

enum class EquipSlotType {
    WEAPON,
    PASSIVE,
    PENALTY
}

sealed interface PassiveCard : Card {
    val equipSlotType: EquipSlotType
}

sealed interface WeaponCard : PassiveCard {
    val range: Int
}