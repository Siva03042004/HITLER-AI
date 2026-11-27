package com.tcprivatelimited.hitlerai.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "council_sessions",
    indices = [Index(value = ["userId", "createdAtMillis"])]
)
data class CouncilSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val prompt: String,
    val edithResponse: String,
    val visionResponse: String,
    val ultronResponse: String,
    val fridayResponse: String,
    val jarvisResponse: String,
    val synthesis: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun turnsMap(): Map<String, String> = linkedMapOf(
        "Edith" to edithResponse,
        "Vision" to visionResponse,
        "Ultron" to ultronResponse,
        "Friday" to fridayResponse,
        "Jarvis" to jarvisResponse
    )
}
