package me.superpenguin.duels.duels

import me.superpenguin.duels.Arena
import me.superpenguin.duels.Duels
import me.superpenguin.duels.kit.Kit
import me.superpenguin.superglue.foundations.Runnables
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

data class Duel(
    val uuid1: UUID,
    val uuid2: UUID,
    var loc1: Location,
    var loc2: Location,
    var inv1: Array<ItemStack>,
    var inv2: Array<ItemStack>,
    val kit: Kit,
    var state: State,
){
    private lateinit var arena: Arena
    private lateinit var bossbar: BossBar
    private lateinit var bartimer: BukkitTask
    private var elapsedSeconds = 0

    val player1 get() = Bukkit.getPlayer(uuid1)
    val player2 get() = Bukkit.getPlayer(uuid2)
    val uuids get() = arrayOf(uuid1, uuid2)

    fun getOtherPlayer(player: Player) = if (player == player1) player2!! else player1!!

    enum class State {
        INVITED, ACCEPTED, STARTING, ONGOING, FINISHED;
    }

    fun start(plugin: Duels, arena: Arena) {
        this.arena = arena
        arena.available = false
        state = State.STARTING
        arena.accept(this)
        kit.applyKit(player1!!, player2!!)
        CountDown(plugin, player1!!, player2!!, this).start()
    }

    fun end(plugin: Duels) {
        state = State.FINISHED
        bartimer.cancel()
        bossbar.removeAll()
        Bukkit.removeBossBar(NamespacedKey(plugin, uuid1.toString()))
        player1?.teleport(loc1)
        player1?.inventory?.contents = inv1
        player2?.teleport(loc2)
        player2?.inventory?.contents = inv2
        arena.available = true
    }

    fun startNewBossBar(plugin: Duels) {
        this.bossbar = Bukkit.createBossBar(
            NamespacedKey(plugin, uuid1.toString()),
            "0:00",
            BarColor.WHITE,
            BarStyle.SOLID,
        )
        uuids.map { Bukkit.getPlayer(it)!! }.forEach(bossbar::addPlayer)
        bartimer = Runnables.runTimer(20) {
            elapsedSeconds++
            bossbar.setTitle("${elapsedSeconds/60}:${(elapsedSeconds%60).toString().padStart(2, '0')}")
        }
    }

    class CountDown(val plugin: Duels, val player1: Player, val player2: Player, val duel: Duel): BukkitRunnable() {
        var secondsRemaining = 5

        override fun run() {
            player1.sendTitle("Starting in $secondsRemaining seconds", "", 0, 20, 0)
            player2.sendTitle("Starting in $secondsRemaining seconds", "", 0, 20, 0)
            if (secondsRemaining-- <= 0) {
                cancel()
                duel.state = State.ONGOING
                duel.startNewBossBar(plugin)
            }
        }

        fun start() {
            runTaskTimer(plugin, 0, 20)
        }
    }

}