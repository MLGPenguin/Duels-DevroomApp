package me.superpenguin.duels.data

import me.superpenguin.duels.Duels
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.*
import kotlin.collections.HashMap

class MySQL(val plugin: Duels): Storage {
    private val config = plugin.config
    private val host = config.getString("sql.host")
    private val port = config.getInt("sql.port")
    private val database = config.getString("sql.database")
    private val username = config.getString("sql.username")
    private val password = config.getString("sql.password")

    val data: HashMap<UUID, PlayerData> = hashMapOf()

    private lateinit var connection: Connection

    override fun setup() {
        runCatching {
            connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$database", username, password)
            connection.prepareStatement(createDataTable).use { it.executeUpdate() }
            plugin.logger.info("Connected to database!")
        }.onFailure {
            plugin.logger.severe("Failed to connect to database!")
            it.printStackTrace()
        }

    }

    override fun shutdown() {
        saveAllData()
        connection.close()
    }

    private fun dataFromDB(uuid: UUID) = connection.prepareStatement(getDataStatement(uuid)).use {
        val rs = it.executeQuery()
        if (rs.next()) PlayerData(
            uuid,
            rs.getInt("kills"),
            rs.getInt("deaths"),
            rs.getInt("wins"),
            rs.getInt("losses")
        ) else null
    }


    override fun loadData(uuid: UUID, async: Boolean) {
        val code: () -> Unit = {
            data[uuid] = dataFromDB(uuid) ?: PlayerData.getNewData(uuid)
        }
        if (async) Bukkit.getScheduler().runTaskAsynchronously(plugin, code) else code.invoke()
    }

    override fun unloadData(player: Player, async: Boolean) {
        val code: () -> Unit = {
            saveData(player)
            data.remove(player.uniqueId)
        }
        if (async) Bukkit.getScheduler().runTaskAsynchronously(plugin, code) else code.invoke()
    }

    override fun getData(uuid: UUID): PlayerData {
        if (!data.containsKey(uuid)) loadData(uuid, false)
        return data[uuid]!!
    }

    override fun getOfflineData(uuid: UUID, callback: (PlayerData?) -> Unit) {
        if (data[uuid] != null) { // If Player is online
            callback.invoke(data[uuid])
        } else Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            callback.invoke(dataFromDB(uuid))
        })
    }

    private fun saveData(vararg uuids: UUID) {
        connection.prepareStatement(getSaveDataStatement()).use {
            for (uuid in uuids) {
                it.applyPlayerDataVariables(data[uuid] ?: continue)
                it.addBatch()
            }
            it.executeBatch()
        }
    }

    override fun saveData(vararg players: Player) {
        saveData(*players.map { it.uniqueId }.toTypedArray())
    }

    override fun saveAllData() {
        saveData(*data.keys.toTypedArray())
    }

    private companion object {
        const val table = "player_data"

        val createDataTable = """
            CREATE TABLE IF NOT EXISTS $table (
                uuid CHAR(36) PRIMARY KEY,
                kills INT, 
                deaths INT,
                wins INT,
                losses INT
            );
        """.trimIndent()

        fun getDataStatement(uuid: UUID) = """
            SELECT * FROM $table
             WHERE uuid = '$uuid'
        """.trimIndent()

        fun getSaveDataStatement() =  """
                INSERT INTO $table VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    kills = VALUES(kills),
                    deaths = VALUES(deaths),
                    wins = VALUES(wins),
                    losses = VALUES(losses);                        
            """.trimIndent()


        fun PreparedStatement.applyPlayerDataVariables(data: PlayerData) = apply {
            setString(1, data.uuid.toString())
            setInt(2, data.kills)
            setInt(3, data.deaths)
            setInt(4, data.wins)
            setInt(5, data.losses)
        }


    }
}