package org.ReDiego0.saloonBetrayal

import org.ReDiego0.saloonBetrayal.manager.ArenaManager
import org.ReDiego0.saloonBetrayal.manager.LanguageManager
import org.ReDiego0.saloonBetrayal.manager.RoleManager
import org.bukkit.plugin.java.JavaPlugin

class SaloonBetrayal : JavaPlugin() {

    companion object {
        lateinit var instance: SaloonBetrayal
            private set
    }

    lateinit var arenaManager: ArenaManager
    lateinit var languageManager: LanguageManager
    lateinit var roleManager: RoleManager

    override fun onEnable() {
        instance = this
        arenaManager = ArenaManager()
        languageManager = LanguageManager(this)
        roleManager = RoleManager()

        saveDefaultConfig()
        registerCommands()
        registerListeners()
    }

    override fun onDisable() {

    }

    private fun registerCommands() {

    }

    private fun registerListeners() {

    }
}