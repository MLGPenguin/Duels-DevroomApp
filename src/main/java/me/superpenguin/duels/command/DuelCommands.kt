package me.superpenguin.duels.command

import com.github.supergluelib.lamp.annotations.NotSelf
import me.superpenguin.duels.Duels
import me.superpenguin.duels.duels.Duel
import me.superpenguin.superglue.foundations.Runnables
import me.superpenguin.superglue.foundations.send
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.exception.CommandErrorException


class DuelCommands(val plugin: Duels) {
    val kitFile get() = plugin.data.kitFile
    val invites = HashMap<Duel, Long>()
    val duelManager = plugin.data.duelManager

    init {
        plugin.data.cmdHandler.autoCompleter.registerSuggestion("invites") {
                _, actor, _ ->  invites.keys.filter { it.uuid2 == actor.uniqueId }.mapNotNull { it.player2?.name }
        }

        Runnables.runTimer(20 * 4) {
            val time = System.currentTimeMillis()
            val entries = invites.entries.filter { it.value <= time }.onEach {
                val duel = it.key
                duel.player1?.send("&7Your duel invite to ${Bukkit.getOfflinePlayer(duel.uuid2).name} has expired")
                duel.player2?.send("&7Your duel invite from ${duel.player1!!.name} has expired")
            }
            invites.entries.removeAll(entries)
        }
    }

    fun clearInvitesFrom(player: Player) {
        invites.entries.removeIf { it.key.player1 == player }
    }

    @Command("duel")
    @AutoComplete("* @Kits")
    fun sendDuel(sender: Player, @NotSelf target: Player, @Default("default") kitName: String) {
        if (duelManager.getDuel(sender) != null) throw CommandErrorException("You are already in a duel")
        val kit = (if (kitName == "default") kitFile.defaultKit else kitFile.getKit(kitName)) ?: throw CommandErrorException("$kitName is not a valid kit")
        if (invites.keys.any { it.player1 == sender && it.player2 == target }) throw CommandErrorException("You have already sent a duel request to this player")
        val duel = Duel(sender.uniqueId, target.uniqueId, sender.location, target.location, sender.inventory.contents, target.inventory.contents, kit, Duel.State.INVITED)
        invites.put(duel, System.currentTimeMillis() + 60_000)
        sender.send("&7Sent a duel request to ${target.name}")
        target.send("&7${sender.name} has invited you to a ${kit.name} duel, type /accept ${sender.name} to accept this invite.")
    }

    @Command("accept")
    fun acceptInvite(sender: Player, target: Player) {
        if (duelManager.getDuel(sender) != null) throw CommandErrorException("You are already in a duel")
        if (duelManager.getDuel(target) != null) throw CommandErrorException("${target.name} is currently in a duel")
        val duel = invites.keys.find { it.uuid1 == target.uniqueId && it.uuid2 == sender.uniqueId }
        if (duel != null) {
            invites.remove(duel)
            duel.state = Duel.State.ACCEPTED
            duel.loc1 = target.location
            duel.loc2 = sender.location
            duel.inv1 = target.inventory.contents
            duel.inv2 = sender.inventory.contents
            duelManager.queueDuel(duel)
            sender.send("&7Accepted ${target.name}'s duel request")
            target.send("${sender.name} has accepted your duel request!")
        } else throw CommandErrorException("You do not have a duel request from this person")
    }

    @Command("stats")
    fun getStats(sender: Player, @Default("me") target: OfflinePlayer) {
        plugin.data.storage.getOfflineData(target.uniqueId) {
            if (it == null) {
                sender.send("&cCould not find data for that player")
                return@getOfflineData
            }
            sender.send("&6${target.name}'s stats")
            sender.send("&7Wins: &6${it.wins}")
            sender.send("&7Kills: &6${it.kills}")
            sender.send("&7Losses: &6${it.losses}")
            sender.send("&7Deaths: &6${it.deaths}")
        }
    }


}