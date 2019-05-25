package com.ajsnarr.choosewhatyoumute.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [App::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract val appDAO: AppDAO

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // lock to make sure no simultaneous access
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "selectivemuteapps.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return INSTANCE!!
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}