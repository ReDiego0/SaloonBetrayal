package org.ReDiego0.saloonBetrayal

import org.bukkit.plugin.java.JavaPlugin

class SaloonBetrayal : JavaPlugin() {

    companion object {
        lateinit var instance: SaloonBetrayal
            private set
    }

    override fun onEnable() {
        instance = this
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