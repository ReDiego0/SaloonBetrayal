package org.ReDiego0.saloonBetrayal.game.character

sealed class PlayerCharacter(
    val id: String,
    val namePath: String,
    val descriptionPath: String,
    val baseHealth: Int
) {
    data object BartCassidy : PlayerCharacter("bart_cassidy", "characters.bart_cassidy.name", "characters.bart_cassidy.desc", 4)
    data object CalamityJanet : PlayerCharacter("calamity_janet", "characters.calamity_janet.name", "characters.calamity_janet.desc", 4)
    data object ElGringo : PlayerCharacter("el_gringo", "characters.el_gringo.name", "characters.el_gringo.desc", 3)
    data object PaulRegret : PlayerCharacter("paul_regret", "characters.paul_regret.name", "characters.paul_regret.desc", 3)
    data object WillyTheKid : PlayerCharacter("willy_the_kid", "characters.willy_the_kid.name", "characters.willy_the_kid.desc", 4)
}