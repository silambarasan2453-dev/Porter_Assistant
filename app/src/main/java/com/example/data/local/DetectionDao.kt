package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionDao {

    @Query("SELECT * FROM detection_records ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectionRecord>>

    @Query("SELECT * FROM detection_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestDetection(): Flow<DetectionRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DetectionRecord): Long

    @Query("DELETE FROM detection_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM detection_records")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM detection_records")
    suspend fun getCount(): Int
}
