package com.example.studienarbeitfoxylibrary.repository.database

import android.app.Application
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Book::class],
    version = 2,
    exportSchema = false
)
abstract class BookDataBase : RoomDatabase() {
    abstract val bookDao: BookDao

    companion object {
        @Volatile
        private var INSTANCE: BookDataBase? = null

        fun getInstance(application: Application): BookDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application.applicationContext,
                    BookDataBase::class.java,
                    "book_database"
                ).fallbackToDestructiveMigration().build()
                instance.getOpenHelper().writableDatabase;
                INSTANCE = instance
                Log.d("BookDatabase", "Database instance created successfully")
                instance
            }
        }
    }
}
