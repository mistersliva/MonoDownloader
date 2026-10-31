package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedTrackDao {

    @Query("SELECT * FROM downloaded_tracks ORDER BY downloadedAt DESC")
    fun getAllDownloadedTracks(): Flow<List<DownloadedTrackEntity>>

    @Query("SELECT * FROM downloaded_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): DownloadedTrackEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_tracks WHERE id = :id)")
    fun isTrackDownloaded(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: DownloadedTrackEntity)

    @Delete
    suspend fun deleteTrack(track: DownloadedTrackEntity)

    @Query("DELETE FROM downloaded_tracks WHERE id = :id")
    suspend fun deleteTrackById(id: String)
}
