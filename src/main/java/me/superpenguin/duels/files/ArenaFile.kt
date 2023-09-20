package me.superpenguin.duels.files

import com.google.gson.Gson
import me.superpenguin.duels.Arena
import me.superpenguin.duels.Duels

/**
 * Handles all interactions with the arenas.json
 */
class ArenaFile(plugin: Duels): JsonConfig<Array<Arena>>(plugin, "arenas.json", true) {
    val gson = Gson().newBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Arena::class.java, Arena.GsonAdapter())
        .create()

    val arenas: Array<Arena>

    init {
        arenas = file.reader().use { gson.fromJson(it, Array<Arena>::class.java) } ?: arrayOf()
    }

    fun nextAvailableArena() = arenas.find { it.available }

}