package org.ReDiego0.saloonBetrayal.manager

import org.ReDiego0.saloonBetrayal.game.character.PlayerCharacter
import org.bukkit.entity.Player

class CharacterManager {

    private val availableCharacters = listOf(
        PlayerCharacter.BartCassidy,
        PlayerCharacter.CalamityJanet,
        PlayerCharacter.ElGringo,
        PlayerCharacter.PaulRegret
    )

    fun assignCharacters(players: List<Player>): Map<Player, PlayerCharacter> {
        val shuffledCharacters = availableCharacters.shuffled()
        return players.zip(shuffledCharacters).toMap()
    }
}