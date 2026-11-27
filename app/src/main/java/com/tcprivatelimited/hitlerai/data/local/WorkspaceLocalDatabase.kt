package com.tcprivatelimited.hitlerai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CouncilSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WorkspaceLocalDatabase : RoomDatabase() {
    abstract fun councilSessionDao(): CouncilSessionDao

    companion object {
        @Volatile
        private var INSTANCE: WorkspaceLocalDatabase? = null

        fun getInstance(context: Context): WorkspaceLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkspaceLocalDatabase::class.java,
                    "hitler_ai_council.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
