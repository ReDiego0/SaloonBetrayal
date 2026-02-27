package org.ReDiego0.saloonBetrayal.game.card

data object VolcanicCard : WeaponCard {
    override val id = "volcanic"
    override val namePath = "cards.volcanic.name"
    override val descriptionPath = "cards.volcanic.desc"
    override val equipSlotType = EquipSlotType.WEAPON
    override val range = 1
}

data object SchofieldCard : WeaponCard {
    override val id = "schofield"
    override val namePath = "cards.schofield.name"
    override val descriptionPath = "cards.schofield.desc"
    override val equipSlotType = EquipSlotType.WEAPON
    override val range = 2
}

data object RemingtonCard : WeaponCard {
    override val id = "remington"
    override val namePath = "cards.remington.name"
    override val descriptionPath = "cards.remington.desc"
    override val equipSlotType = EquipSlotType.WEAPON
    override val range = 3
}

data object RevCarabineCard : WeaponCard {
    override val id = "rev_carabine"
    override val namePath = "cards.rev_carabine.name"
    override val descriptionPath = "cards.rev_carabine.desc"
    override val equipSlotType = EquipSlotType.WEAPON
    override val range = 4
}

data object WinchesterCard : WeaponCard {
    override val id = "winchester"
    override val namePath = "cards.winchester.name"
    override val descriptionPath = "cards.winchester.desc"
    override val equipSlotType = EquipSlotType.WEAPON
    override val range = 5
}

data object MustangCard : PassiveCard {
    override val id = "mustang"
    override val namePath = "cards.mustang.name"
    override val descriptionPath = "cards.mustang.desc"
    override val equipSlotType = EquipSlotType.PASSIVE
}

data object ScopeCard : PassiveCard {
    override val id = "scope"
    override val namePath = "cards.scope.name"
    override val descriptionPath = "cards.scope.desc"
    override val equipSlotType = EquipSlotType.PASSIVE
}

data object BarrelCard : PassiveCard {
    override val id = "barrel"
    override val namePath = "cards.barrel.name"
    override val descriptionPath = "cards.barrel.desc"
    override val equipSlotType = EquipSlotType.PASSIVE
}

data object DynamiteCard : PassiveCard {
    override val id = "dynamite"
    override val namePath = "cards.dynamite.name"
    override val descriptionPath = "cards.dynamite.desc"
    override val equipSlotType = EquipSlotType.PENALTY
}