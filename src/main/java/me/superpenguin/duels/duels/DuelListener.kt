package me.superpenguin.duels.duels

import me.superpenguin.duels.Duels
import me.superpenguin.duels.command.DuelCommands
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

class DuelListener(plugin: Duels, val duelCommand: DuelCommands): Listener {
    val duelManager = plugin.data.duelManager

    @EventHandler fun onDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity
        val attacker = event.damager
        if (victim !is Player || attacker !is Player || !duelManager.areDueling(victim, attacker)) return
        val duel = duelManager.getDuel(victim)!!
        // Now we've established both players are in a duel together
        if (duel.state != Duel.State.ONGOING) {
            event.isCancelled = true
            return
        }

        if (event.finalDamage >= victim.health) {
            event.isCancelled = true
            duelManager.endDuel(duel, attacker)
        }
    }


    @EventHandler fun onDeath(event: PlayerDeathEvent) {
        val duel = duelManager.getDuel(event.entity) ?: return
        duelManager.endDuel(duel, duel.getOtherPlayer(event.entity))
    }

    @EventHandler fun onQuit(event: PlayerQuitEvent) {
        duelCommand.clearInvitesFrom(event.player)
        val duel = duelManager.getDuel(event.player) ?: return
        duelManager.endDuel(duel, duel.getOtherPlayer(event.player))
    }
}