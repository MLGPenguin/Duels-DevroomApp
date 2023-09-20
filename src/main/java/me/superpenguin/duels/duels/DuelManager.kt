package me.superpenguin.duels.duels

import me.superpenguin.duels.Duels
import me.superpenguin.duels.data.Storage
import me.superpenguin.duels.files.ArenaFile
import me.superpenguin.superglue.foundations.Runnables
import me.superpenguin.superglue.foundations.send
import me.superpenguin.superglue.foundations.toColour
import org.bukkit.entity.Player
import java.util.*

class DuelManager(val plugin: Duels, val arenaFile: ArenaFile, val data: Storage) {
    val queue: Queue<Duel> = LinkedList()
    val duels = arrayListOf<Duel>()
    fun getDuel(player: Player) = duels.find { player.uniqueId in it.uuids }
    fun areDueling(p1: Player, p2: Player) = getDuel(p1).let { it != null && p2.uniqueId in it.uuids }

    init {
        Runnables.runTimer(20 * 5) {
            if (queue.isEmpty()) return@runTimer
            val arena = arenaFile.nextAvailableArena()
            if (arena != null) {
                queue.poll().start(plugin, arena)
            }
        }
    }

    /**
     * Once a duel has been accepted, it should be passed to this method where it will queue the duel to start as soon as there is an available arena.
     */
    fun queueDuel(duel: Duel) {
        val arena = arenaFile.nextAvailableArena()
        duels.add(duel)
        if (arena == null) queue.add(duel)
        else duel.start(plugin, arena)
    }

    fun endDuel(duel: Duel, winner: Player) {
        duel.end(plugin)
        duels.remove(duel)
        val loser = duel.getOtherPlayer(winner)
        winner.health = 20.0
        loser.health = 20.0
        winner.send("&aYou have won the duel!")
        winner.sendTitle("&aYou have won!".toColour(), "")
        loser.send("&cYou have lost the duel!")
        loser.sendTitle("&cYou have lost!".toColour(), "")
        data.getData(winner).apply {
            wins++
            kills++
        }
        data.getData(loser).apply {
            losses++
            deaths++
        }
        data.saveDataAsync(plugin, winner, loser)
    }


}