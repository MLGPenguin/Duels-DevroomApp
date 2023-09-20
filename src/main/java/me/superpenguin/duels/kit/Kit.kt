package me.superpenguin.duels.kit

import org.bukkit.entity.Player

data class Kit(
    val name: String,
    val armor_content: KitArmor,
    val inventory_content: Array<InventoryItem>,
) {

    fun applyKit(vararg players: Player) {
        for (player in players) {
            player.inventory.clear()
            player.inventory.setArmorContents(armor_content.getArmorContents())
            inventory_content.forEach { it.setToInventory(player.inventory) }
        }
    }
}