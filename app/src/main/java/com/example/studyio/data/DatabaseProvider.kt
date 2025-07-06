package com.example.studyio.data

import android.content.Context
import com.example.studyio.data.entities.StudyioDatabase
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: StudyioDatabase? = null

    fun getDatabase(context: Context): StudyioDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context,
                StudyioDatabase::class.java,
                "studyio.db"
            ).fallbackToDestructiveMigration(true)
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .build()
                .also { INSTANCE = it }
        }
    }
}