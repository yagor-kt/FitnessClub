package com.example.fitnessclub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.fitnessclub.data.local.dao.BookingDao
import com.example.fitnessclub.data.local.dao.TrainerDao
import com.example.fitnessclub.data.local.dao.UserDao
import com.example.fitnessclub.data.local.dao.WorkoutDao
import com.example.fitnessclub.data.local.entity.Booking
import com.example.fitnessclub.data.local.entity.Trainer
import com.example.fitnessclub.data.local.entity.User
import com.example.fitnessclub.data.local.entity.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [User::class, Workout::class, Booking::class, Trainer::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun bookingDao(): BookingDao
    abstract fun trainerDao(): TrainerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        private fun buildDatabase(context: Context): AppDatabase {
            lateinit var database: AppDatabase

            database = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "fitness_club_database"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)

                        CoroutineScope(Dispatchers.IO).launch {
                            database.seedInitialData()
                        }
                    }
                })
                .build()

            return database
        }

        private suspend fun AppDatabase.seedInitialData() {
            withTransaction {
                val now = System.currentTimeMillis()

                userDao().insert(
                    User(
                        email = "test@test.ru",
                        password = "1234",
                        name = "Алексей",
                        subscriptionEnd = now + 30L * 24 * 60 * 60 * 1000
                    )
                )

                val trainers = listOf(
                    Trainer(
                        name = "Анна Смирнова",
                        specialization = "Йога и растяжка",
                        experienceYears = 7,
                        photoUrl = "https://i.pravatar.cc/300?img=12"
                    ),
                    Trainer(
                        name = "Михаил Иванов",
                        specialization = "Силовые тренировки",
                        experienceYears = 10,
                        photoUrl = "https://i.pravatar.cc/300?img=13"
                    ),
                    Trainer(
                        name = "Елена Петрова",
                        specialization = "Функциональный тренинг",
                        experienceYears = 5,
                        photoUrl = "https://i.pravatar.cc/300?img=14"
                    )
                )
                trainerDao().insertAll(trainers)

                val workoutTitles = listOf(
                    "Йога",
                    "Силовая тренировка",
                    "Функциональный тренинг",
                    "Пилатес",
                    "Круговая тренировка"
                )
                val workoutTrainers = listOf(
                    trainers[0].name,
                    trainers[1].name,
                    trainers[2].name,
                    trainers[0].name,
                    trainers[1].name
                )

                val workouts = workoutTitles.indices.map { index ->
                    val date = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, index + 1)
                        set(Calendar.HOUR_OF_DAY, 9 + index)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    Workout(
                        title = workoutTitles[index],
                        trainerName = workoutTrainers[index],
                        dateTime = date,
                        hall = "Зал ${index + 1}",
                        maxCapacity = 12,
                        currentBookings = index % 3,
                        isPersonal = false
                    )
                }
                workouts.forEach { workoutDao().insert(it) }
            }
        }
    }
}