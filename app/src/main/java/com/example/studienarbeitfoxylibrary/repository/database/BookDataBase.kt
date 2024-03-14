package com.example.studienarbeitfoxylibrary.repository.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Book::class],
    version = 2,
    exportSchema = false
)
abstract class BookDataBase: RoomDatabase() {
    abstract val bookDao: BookDao

    companion object
    {
        @Volatile
        private var INSTANCE: BookDataBase? = null

        fun createInstance(application: Application): BookDataBase
        {
            synchronized(this)
            {
                var instance = INSTANCE
                if (instance == null)
                {
                    instance = Room.databaseBuilder(
                        application.applicationContext,
                        BookDataBase::class.java,
                        "book_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}