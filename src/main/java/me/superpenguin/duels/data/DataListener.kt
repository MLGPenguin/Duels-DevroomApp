package me.superpenguin.duels.data

import me.superpenguin.duels.Duels
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class DataListener(val plugin: Duels): Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        plugin.data.storage.loadData(event.player.uniqueId, true)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        plugin.data.storage.unloadData(event.player, true)
    }

}