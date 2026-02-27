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
        repeat(12) { drawPile.add(createGameCard(MissedCard)) }
        repeat(6) { drawPile.add(createGameCard(BeerCard)) }
        repeat(4) { drawPile.add(createGameCard(PanicCard)) }
        repeat(4) { drawPile.add(createGameCard(CatBalouCard)) }
        repeat(3) { drawPile.add(createGameCard(DuelCard)) }
        repeat(2) { drawPile.add(createGameCard(IndiansCard)) }
        repeat(1) { drawPile.add(createGameCard(GatlingCard)) }
        repeat(1) { drawPile.add(createGameCard(SaloonCard)) }
        repeat(2) { drawPile.add(createGameCard(StagecoachCard)) }
        repeat(1) { drawPile.add(createGameCard(WellsFargoCard)) }
        repeat(2) { drawPile.add(createGameCard(GeneralStoreCard)) }
        repeat(2) { drawPile.add(createGameCard(BarrelCard)) }
        repeat(3) { drawPile.add(createGameCard(JailCard)) }
        repeat(1) { drawPile.add(createGameCard(DynamiteCard)) }
        repeat(2) { drawPile.add(createGameCard(MustangCard)) }
        repeat(1) { drawPile.add(createGameCard(ScopeCard)) }
        repeat(2) { drawPile.add(createGameCard(VolcanicCard)) }
        repeat(3) { drawPile.add(createGameCard(SchofieldCard)) }
        repeat(1) { drawPile.add(createGameCard(RemingtonCard)) }
        repeat(1) { drawPile.add(createGameCard(RevCarabineCard)) }
        repeat(1) { drawPile.add(createGameCard(WinchesterCard)) }

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