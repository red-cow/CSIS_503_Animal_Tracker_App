package com.example.animalsalestracker.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [Animal::class, Customer::class, Order::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AnimalSales-db"
                )
                    // Wipes and rebuilds if no migration found
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
//        suspend fun resetDatabase(context: Context) {
//            val db = get(context)
//            // run on IO so we don't block main
//            withContext(Dispatchers.IO) {
//                db.clearAllTables()
//            }
//        }
    }
}