package org.ReDiego0.saloonBetrayal.manager

import org.ReDiego0.saloonBetrayal.game.role.Role
import org.bukkit.entity.Player

class RoleManager {

    fun assignRoles(players: List<Player>): Map<Player, Role> {
        val rolePool = mutableListOf<Role>()
        val size = players.size

        rolePool.add(Role.Sheriff)
        rolePool.add(Role.Renegade)
        rolePool.add(Role.Outlaw)
        rolePool.add(Role.Outlaw)

        if (size >= 5) rolePool.add(Role.Deputy)
        if (size >= 6) rolePool.add(Role.Outlaw)
        if (size >= 7) rolePool.add(Role.Deputy)

        rolePool.shuffle()

        return players.zip(rolePool).toMap()
    }
}