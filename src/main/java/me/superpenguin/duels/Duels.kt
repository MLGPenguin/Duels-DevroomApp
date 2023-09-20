package me.superpenguin.duels

import com.github.supergluelib.lamp.LampManager
import me.superpenguin.duels.command.DuelCommands
import me.superpenguin.duels.data.DataListener
import me.superpenguin.duels.data.MySQL
import me.superpenguin.duels.data.Storage
import me.superpenguin.duels.duels.DuelListener
import me.superpenguin.duels.duels.DuelManager
import me.superpenguin.duels.files.ArenaFile
import me.superpenguin.duels.files.KitFile
import me.superpenguin.superglue.foundations.Runnables
import me.superpenguin.superglue.foundations.register
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler

class Duels: JavaPlugin() {

    data class Data(
        val cmdHandler: BukkitCommandHandler,
        val kitFile: KitFile,
        val arenaFile: ArenaFile,
        val duelManager: DuelManager,
        val storage: Storage,
    )

    lateinit var data: Data
        private set

    override fun onEnable() {
        saveDefaultConfig()

        Runnables.setup(this)

        val cmdhandler = LampManager.create(this)
        val kitFile = KitFile(this)
        val arenaFile = ArenaFile(this)
        val storage = MySQL(this)
        val duelManager = DuelManager(this, arenaFile, storage)

        data = Data(
            cmdhandler,
            kitFile,
            arenaFile,
            duelManager,
            storage,
        )

        storage.setup()

        cmdhandler.autoCompleter.registerSuggestion("Kits") { _, _, _ -> data.kitFile.kits.map { it.name } }
        val duelCmd = DuelCommands(this)
        cmdhandler.register(duelCmd)


        DuelListener(this, duelCmd).register(this)
        DataListener(this).register(this)
    }

    override fun onDisable() {
        data.storage.shutdown()
    }

}