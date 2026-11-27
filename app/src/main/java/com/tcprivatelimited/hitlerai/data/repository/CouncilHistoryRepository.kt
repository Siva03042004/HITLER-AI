package com.tcprivatelimited.hitlerai.data.repository

import android.content.Context
import com.tcprivatelimited.hitlerai.data.local.CouncilSessionDao
import com.tcprivatelimited.hitlerai.data.local.CouncilSessionEntity
import com.tcprivatelimited.hitlerai.data.local.WorkspaceLocalDatabase
import kotlinx.coroutines.flow.Flow

class CouncilHistoryRepository(private val dao: CouncilSessionDao) {

    constructor(context: Context) : this(
        WorkspaceLocalDatabase.getInstance(context.applicationContext).councilSessionDao()
    )

    fun observeSessions(userId: String, query: String): Flow<List<CouncilSessionEntity>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            dao.observeSessionsForUser(userId)
        } else {
            dao.searchSessionsForUser(userId, trimmed)
        }
    }

    suspend fun saveSession(
        userId: String,
        prompt: String,
        turns: Map<String, String>,
        synthesis: String
    ): CouncilSessionEntity {
        val entity = CouncilSessionEntity(
            userId = userId,
            prompt = prompt.trim(),
            edithResponse = turns["Edith"].orEmpty(),
            visionResponse = turns["Vision"].orEmpty(),
            ultronResponse = turns["Ultron"].orEmpty(),
            fridayResponse = turns["Friday"].orEmpty(),
            jarvisResponse = turns["Jarvis"].orEmpty(),
            synthesis = synthesis,
            createdAtMillis = System.currentTimeMillis()
        )
        val generatedId = dao.insertSession(entity)
        return entity.copy(id = generatedId)
    }

    suspend fun deleteSession(sessionId: Long, userId: String) {
        dao.deleteSessionById(sessionId, userId)
    }
}
