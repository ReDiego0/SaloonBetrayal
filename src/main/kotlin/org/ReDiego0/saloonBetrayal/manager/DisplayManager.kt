package org.ReDiego0.saloonBetrayal.manager

import net.kyori.adventure.text.Component
import org.ReDiego0.saloonBetrayal.game.Arena
import org.ReDiego0.saloonBetrayal.game.role.Role
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay

class DisplayManager(private val languageManager: LanguageManager) {

    private val playerDisplays = mutableMapOf<Player, TextDisplay>()

    fun setupDisplayForPlayer(player: Player, arena: Arena, positionConfig: String = "ABOVE") {
        val seatLocation = player.location

        val displayLocation = when (positionConfig.uppercase()) {
            "BESIDE" -> {
                val direction = seatLocation.direction
                val rightVector = direction.clone().crossProduct(org.bukkit.util.Vector(0, 1, 0)).normalize()
                seatLocation.clone().add(rightVector.multiply(1.2)).add(0.0, 1.0, 0.0)
            }
            "ABOVE" -> seatLocation.clone().add(0.0, 2.2, 0.0)
            else -> seatLocation.clone().add(0.0, 2.2, 0.0)
        }

        val textDisplay = seatLocation.world.spawn(displayLocation, TextDisplay::class.java) { display ->
            display.billboard = Display.Billboard.CENTER
            display.isPersistent = false
            display.backgroundColor = org.bukkit.Color.fromARGB(100, 0, 0, 0)
        }

        playerDisplays[player] = textDisplay
        updateDisplay(player, arena)
    }

    fun updateDisplay(player: Player, arena: Arena) {
        val display = playerDisplays[player] ?: return
        val role = arena.playerRoles[player]
        val character = arena.playerCharacters[player] ?: return

        val maxHealth = arena.getPlayerMaxHealth(player)
        val currentHealth = player.health.toInt() / 2

        var textComponent = languageManager.getMessage(character.namePath).append(Component.newline())

        if (role == Role.Sheriff) {
            textComponent = textComponent.append(languageManager.getMessage(role.namePath)).append(Component.newline())
        }

        val heartsStr = buildHearts(currentHealth, maxHealth)
        textComponent = textComponent.append(languageManager.getMessage("displays.health_format", "hearts" to heartsStr))

        display.text(textComponent)
    }

    fun removeDisplay(player: Player) {
        playerDisplays[player]?.remove()
        playerDisplays.remove(player)
    }

    fun clearAll() {
        playerDisplays.values.forEach { it.remove() }
        playerDisplays.clear()
    }

    private fun buildHearts(current: Int, max: Int): String {
        val safeCurrent = current.coerceIn(0, max)
        val fullHearts = "❤".repeat(safeCurrent)
        val emptyHearts = "♡".repeat(max - safeCurrent)
        return "<red>$fullHearts</red><gray>$emptyHearts</gray>"
    }
}