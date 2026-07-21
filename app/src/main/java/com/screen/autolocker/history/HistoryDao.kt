package com.screen.autolocker.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query(
        """
        SELECT * FROM history_entries
        WHERE (:statusFilter IS NULL OR status = :statusFilter OR (:statusFilter = 'Stopped' AND status LIKE 'Stopped%'))
        AND (:query = '' OR CAST(minutes AS TEXT) LIKE '%' || :query || '%' OR status LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
        """
    )
    fun observeFiltered(query: String, statusFilter: String?): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    suspend fun getAll(): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<HistoryEntity>)

    @Query("DELETE FROM history_entries")
    suspend fun clear()
}
