package org.ReDiego0.saloonBetrayal.game.card

class Deck {
    private val drawPile = mutableListOf<Card>()
    private val discardPile = mutableListOf<Card>()

    fun initializeBaseDeck() {
        drawPile.clear()
        discardPile.clear()

        repeat(25) { drawPile.add(BangCard) }
        repeat(6) { drawPile.add(BeerCard) }
        repeat(2) { drawPile.add(StagecoachCard) }

        drawPile.shuffle()
    }

    fun draw(): Card {
        if (drawPile.isEmpty()) {
            reshuffleDiscardIntoDraw()
        }
        return drawPile.removeLast()
    }

    fun discard(card: Card) {
        discardPile.add(card)
    }

    private fun reshuffleDiscardIntoDraw() {
        drawPile.addAll(discardPile)
        discardPile.clear()
        drawPile.shuffle()
    }

    fun getDrawPileSize(): Int = drawPile.size
}