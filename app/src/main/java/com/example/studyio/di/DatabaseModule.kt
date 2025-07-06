package com.example.studyio.di

import android.content.Context
import com.example.studyio.data.DeckRepository
import com.example.studyio.data.entities.DeckDao
import com.example.studyio.data.entities.StudyioDatabase
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
        com.example.studyio.data.entities.buildStudyioDatabase(context)

    @Provides
    fun provideDeckDao(db: StudyioDatabase): DeckDao = db.deckDao()

    @Provides
    @Singleton
    fun provideDeckRepository(deckDao: DeckDao): DeckRepository = DeckRepository(deckDao)
}

