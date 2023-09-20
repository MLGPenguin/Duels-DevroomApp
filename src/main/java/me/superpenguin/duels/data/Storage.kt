package me.superpenguin.duels.data

import me.superpenguin.duels.Duels
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

interface Storage {
    fun setup()
    fun shutdown()

    fun loadData(uuid: UUID, async: Boolean)
    fun unloadData(player: Player, async: Boolean)

    fun getData(uuid: UUID): PlayerData
    fun getOfflineData(uuid: UUID, callback: (PlayerData?) -> Unit)
    fun saveData(vararg players: Player)
    fun saveAllData()

    fun saveAllDataAsync(plugin: Duels) = Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable { saveAllData() })
    fun saveDataAsync(plugin: Duels, vararg players: Player) = Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable { saveData(*players) })
    fun getData(player: Player): PlayerData = getData(player.uniqueId)

}