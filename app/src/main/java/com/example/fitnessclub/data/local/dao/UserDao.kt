package com.example.fitnessclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnessclub.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM User WHERE id = :userId LIMIT 1")
    suspend fun getById(userId: Long): User?

    @Query("SELECT * FROM User WHERE id = :userId LIMIT 1")
    fun observeById(userId: Long): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User): Int
}