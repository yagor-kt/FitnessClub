package com.example.fitnessclub.data.repository

import com.example.fitnessclub.data.local.dao.BookingDao
import com.example.fitnessclub.data.local.dao.UserDao
import com.example.fitnessclub.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val bookingDao: BookingDao
) {
    suspend fun getByEmail(email: String): User? = userDao.getByEmail(email)

    suspend fun getById(userId: Long): User? = userDao.getById(userId)

    fun observeById(userId: Long): Flow<User?> = userDao.observeById(userId)

    fun monthlyWorkoutCount(
        userId: Long,
        monthStart: Long,
        now: Long
    ): Flow<Int> = bookingDao.countWorkoutsBetween(userId, monthStart, now)

    suspend fun register(email: String, password: String, name: String): Long {
        val now = System.currentTimeMillis()
        return userDao.insert(
            User(
                email = email,
                password = password,
                name = name,
                weight = null,
                goal = null,
                subscriptionEnd = now + 30L * 24 * 60 * 60 * 1000
            )
        )
    }

    suspend fun update(user: User) {
        userDao.update(user)
    }
}