package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FestivalDao {
    @Query("SELECT * FROM festivals ORDER BY nameEnglish ASC")
    fun getAllFestivals(): Flow<List<FestivalEntity>>

    @Query("SELECT * FROM festivals WHERE id = :id LIMIT 1")
    fun getFestivalById(id: String): Flow<FestivalEntity?>

    @Query("SELECT * FROM festivals WHERE id = :id LIMIT 1")
    suspend fun getFestivalByIdDirect(id: String): FestivalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(festivals: List<FestivalEntity>)

    @Query("SELECT COUNT(*) FROM festivals")
    suspend fun getCount(): Int

    @Query("DELETE FROM festivals WHERE id LIKE 'custom_%'")
    suspend fun deleteSynthesizedCustomEntries()
}

@Dao
interface SamagriProgressDao {
    @Query("SELECT * FROM samagri_progress WHERE festivalId = :festivalId AND year = :year LIMIT 1")
    fun getProgress(festivalId: String, year: Int): Flow<SamagriProgressEntity?>

    @Query("SELECT * FROM samagri_progress WHERE festivalId = :festivalId AND year = :year LIMIT 1")
    suspend fun getProgressDirect(festivalId: String, year: Int): SamagriProgressEntity?

    @Query("SELECT * FROM samagri_progress")
    fun getAllProgress(): Flow<List<SamagriProgressEntity>>

    @Query("SELECT * FROM samagri_progress")
    suspend fun getAllProgressList(): List<SamagriProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: SamagriProgressEntity)

    @Query("DELETE FROM samagri_progress")
    suspend fun deleteAll()
}
