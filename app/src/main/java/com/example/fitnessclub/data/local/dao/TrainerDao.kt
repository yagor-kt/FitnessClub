package com.example.fitnessclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitnessclub.data.local.entity.Trainer
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainerDao {
    @Query("SELECT * FROM Trainer ORDER BY name ASC")
    fun getAll(): Flow<List<Trainer>>

    @Insert
    suspend fun insertAll(trainers: List<Trainer>)
}