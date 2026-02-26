package org.ReDiego0.saloonBetrayal.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.ReDiego0.saloonBetrayal.SaloonBetrayal
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class LanguageManager(private val plugin: SaloonBetrayal) {

    private lateinit var config: YamlConfiguration
    private val file = File(plugin.dataFolder, "messages.yml")
    private val miniMessage = MiniMessage.miniMessage()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false)
        }
        config = YamlConfiguration.loadConfiguration(file)
    }

    fun reload() {
        loadConfig()
    }

    fun getMessage(path: String, vararg placeholders: Pair<String, String>): Component {
        var rawMessage = config.getString(path) ?: "<red>Mensaje no encontrado: $path</red>"

        placeholders.forEach { (key, value) ->
            rawMessage = rawMessage.replace("<$key>", value)
        }

        return miniMessage.deserialize(rawMessage)
    }
}