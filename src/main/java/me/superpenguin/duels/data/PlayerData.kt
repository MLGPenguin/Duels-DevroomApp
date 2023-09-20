package me.superpenguin.duels.data

import java.util.*

data class PlayerData(
    val uuid: UUID,
    var kills: Int,
    var deaths: Int,
    var wins: Int,
    var losses: Int,
) {

    companion object {
        fun getNewData(uuid: UUID) = PlayerData(uuid, 0, 0, 0, 0)
    }
}
