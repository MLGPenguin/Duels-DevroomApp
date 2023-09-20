package me.superpenguin.duels.kit

import me.superpenguin.superglue.foundations.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.inventory.Inventory

data class SimpleItemStack(
    val material: Material,
    val amount: Int,
) {
    val item get() = ItemBuilder(material, amount = amount).build()
}

data class KitArmor(
    val helmet: SimpleItemStack?,
    val chestplate: SimpleItemStack?,
    val leggings: SimpleItemStack?,
    val boots: SimpleItemStack?
) {
    fun getArmorContents() = arrayOf(boots?.item, leggings?.item, chestplate?.item, helmet?.item)
}

data class InventoryItem(
    val slot: Int,
    val material: Material,
    val amount: Int
) {
    fun setToInventory(inv: Inventory) = inv.setItem(slot, ItemBuilder(material, amount = amount).build())
}