package me.superpenguin.duels.files

import com.google.gson.Gson
import me.superpenguin.duels.Duels
import me.superpenguin.duels.kit.Kit

class KitFile(plugin: Duels): JsonConfig<KitFile.KitData>(plugin, "kits.json", true) {
    val gson = Gson().newBuilder().setPrettyPrinting().serializeNulls().create()

    data class KitData(
        val default: String,
        val kits: Array<Kit>
    )

    val kitData: KitData
    val kits get() = kitData.kits
    val defaultKit get() = getKit(kitData.default)

    init {
        kitData = file.reader().use { gson.fromJson(it, KitData::class.java) }
    }

    fun getKit(name: String) = kits. find { it.name.equals(name, true) }

}