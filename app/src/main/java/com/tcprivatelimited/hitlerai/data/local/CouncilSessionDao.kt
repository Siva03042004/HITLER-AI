package com.tcprivatelimited.hitlerai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CouncilSessionDao {

    @Query("SELECT * FROM council_sessions WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeSessionsForUser(userId: String): Flow<List<CouncilSessionEntity>>

    @Query(
        """
        SELECT * FROM council_sessions
        WHERE userId = :userId
          AND (prompt LIKE '%' || :query || '%' OR synthesis LIKE '%' || :query || '%')
        ORDER BY createdAtMillis DESC
        """
    )
    fun searchSessionsForUser(userId: String, query: String): Flow<List<CouncilSessionEntity>>

    @Query("SELECT * FROM council_sessions WHERE id = :sessionId AND userId = :userId LIMIT 1")
    suspend fun getSessionById(sessionId: Long, userId: String): CouncilSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CouncilSessionEntity): Long

    @Query("DELETE FROM council_sessions WHERE id = :sessionId AND userId = :userId")
    suspend fun deleteSessionById(sessionId: Long, userId: String)
}
