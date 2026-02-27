package org.ReDiego0.saloonBetrayal.game.role

sealed class Role(val namePath: String, val healthModifier: Int) {
    data object Sheriff : Role("roles.sheriff.name", 1)
    data object Deputy : Role("roles.deputy.name", 0)
    data object Outlaw : Role("roles.outlaw.name", 0)
    data object Renegade : Role("roles.renegade.name", 0)
}