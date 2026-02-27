package org.ReDiego0.saloonBetrayal

import org.ReDiego0.saloonBetrayal.command.SaloonCommand
import org.ReDiego0.saloonBetrayal.listener.CardListener
import org.ReDiego0.saloonBetrayal.listener.HotkeyListener
import org.ReDiego0.saloonBetrayal.listener.SpectatorListener
import org.ReDiego0.saloonBetrayal.manager.*
import org.bukkit.plugin.java.JavaPlugin

class SaloonBetrayal : JavaPlugin() {

    companion object {
        lateinit var instance: SaloonBetrayal
            private set
    }

    lateinit var languageManager: LanguageManager
        private set
    lateinit var arenaManager: ArenaManager
        private set
    lateinit var roleManager: RoleManager
        private set
    lateinit var characterManager: CharacterManager
        private set
    lateinit var guiManager: GUIManager
        private set
    lateinit var displayManager: DisplayManager
        private set
    lateinit var drawCheckManager: DrawCheckManager
        private set

    override fun onEnable() {
        instance = this
        saveDefaultConfig()

        languageManager = LanguageManager(this)
        roleManager = RoleManager()
        characterManager = CharacterManager()
        arenaManager = ArenaManager()
        guiManager = GUIManager(languageManager)
        displayManager = DisplayManager(languageManager)
        drawCheckManager = DrawCheckManager(languageManager)

        registerCommands()
        registerListeners()
    }

    private fun registerCommands() {
        val saloonCommand = SaloonCommand(arenaManager, languageManager)
        getCommand("saloon")?.setExecutor(saloonCommand)
        getCommand("saloon")?.tabCompleter = saloonCommand
    }

    private fun registerListeners() {
        val pm = server.pluginManager
        pm.registerEvents(CardListener(arenaManager), this)
        pm.registerEvents(HotkeyListener(arenaManager, guiManager), this)
        pm.registerEvents(SpectatorListener(arenaManager, languageManager), this)
    }
}