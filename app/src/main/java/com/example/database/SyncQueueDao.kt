package com.example.database

import androidx.room.*
import com.example.data.model.SyncItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getPendingSyncItemsFlow(): Flow<List<SyncItem>>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getPendingSyncItems(): List<SyncItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncItem): Long

    @Delete
    suspend fun deleteSyncItem(item: SyncItem)

    @Query("DELETE FROM sync_queue WHERE recordId = :recordId")
    suspend fun deleteSyncItemForRecord(recordId: Long)
}
