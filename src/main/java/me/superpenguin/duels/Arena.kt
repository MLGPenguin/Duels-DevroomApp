package me.superpenguin.duels

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import me.superpenguin.duels.duels.Duel
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

data class Arena(
    val world: World,
    val spawn1: Location,
    val spawn2: Location,
    @Transient var available: Boolean = true,
) {

    fun accept(duel: Duel) {
        duel.player1?.teleport(spawn1)
        duel.player2?.teleport(spawn2)
    }

    class GsonAdapter: TypeAdapter<Arena>() {
        private fun JsonReader.skipAndNextDouble(): Double {
            nextName()
            return nextDouble()
        }

        override fun write(out: JsonWriter, arena: Arena?) {
            if (arena == null) {
                out.nullValue()
                return
            }

            out.beginObject()
                .name("world").value(arena.world.name)
                .name("player_one_spawn")
                .beginObject()
                .name("x").value(arena.spawn1.x)
                .name("y").value(arena.spawn1.y)
                .name("z").value(arena.spawn1.z)
                .endObject()
                .name("player_two_spawn")
                .beginObject()
                .name("x").value(arena.spawn2.x)
                .name("y").value(arena.spawn2.y)
                .name("z").value(arena.spawn2.z)
                .endObject()
                .endObject()
        }

        override fun read(input: JsonReader): Arena {
            input.beginObject()
            input.nextName()
            val worldname = input.nextString()
            val world = Bukkit.getWorld(worldname) ?: throw NullPointerException("Could not find a loaded world named $worldname")
            input.nextName()
            input.beginObject()
            val loc1 = Location(world, input.skipAndNextDouble(), input.skipAndNextDouble(), input.skipAndNextDouble())
            input.endObject()
            input.nextName()
            input.beginObject()
            val loc2 = Location(world, input.skipAndNextDouble(), input.skipAndNextDouble(), input.skipAndNextDouble())
            input.endObject()
            input.endObject()
            return Arena(world, loc1, loc2)
        }
    }
}
