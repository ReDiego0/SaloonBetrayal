package org.ReDiego0.saloonBetrayal.game.card

class Deck {
    private val drawPile = mutableListOf<GameCard>()
    private val discardPile = mutableListOf<GameCard>()

    fun initializeBaseDeck() {
        drawPile.clear()
        discardPile.clear()

        val suits = Suit.values()
        val ranks = Rank.values()
        var suitIndex = 0
        var rankIndex = 0

        fun createGameCard(base: Card): GameCard {
            val card = GameCard(base, suits[suitIndex % suits.size], ranks[rankIndex % ranks.size])
            suitIndex++
            rankIndex++
            return card
        }

        repeat(25) { drawPile.add(createGameCard(BangCard)) }
        repeat(6) { drawPile.add(createGameCard(BeerCard)) }
        repeat(2) { drawPile.add(createGameCard(StagecoachCard)) }

        drawPile.shuffle()
    }

    fun draw(): GameCard {
        if (drawPile.isEmpty()) {
            reshuffleDiscardIntoDraw()
        }
        return drawPile.removeLast()
    }

    fun discard(card: GameCard) {
        discardPile.add(card)
    }

    private fun reshuffleDiscardIntoDraw() {
        drawPile.addAll(discardPile)
        discardPile.clear()
        drawPile.shuffle()
    }

    fun getDrawPileSize(): Int = drawPile.size
}