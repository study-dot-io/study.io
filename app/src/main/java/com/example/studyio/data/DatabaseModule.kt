package com.example.studyio.data

import android.content.Context
import androidx.room.Room
import com.example.studyio.data.entities.StudyioDatabase
import com.example.studyio.data.entities.DeckDao
import com.example.studyio.data.entities.CardDao
import com.example.studyio.data.entities.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudyioDatabase =
        Room.databaseBuilder(context, StudyioDatabase::class.java, "studyio_db")
            .fallbackToDestructiveMigration(true) // Added for MVP: allows destructive migration during rapid schema changes
            .fallbackToDestructiveMigrationOnDowngrade(true) // Added for MVP: allows destructive migration on downgrade
            .build()

    @Provides
    fun provideDeckDao(database: StudyioDatabase): DeckDao = database.deckDao()

    @Provides
    fun provideCardDao(database: StudyioDatabase): CardDao = database.cardDao()

    @Provides
    fun provideNoteDao(database: StudyioDatabase): NoteDao = database.noteDao()
}
