package com.example.studyio.data.db.module

import android.content.Context
import androidx.room.Room
import com.example.studyio.data.db.AppDatabase
import com.example.studyio.data.db.DAO.DeckDao
import com.example.studyio.data.db.DAO.ReviewCardsDao
import com.example.studyio.data.repository.DeckRepository
import com.example.studyio.data.repository.ReviewCardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideDeckDao(database: AppDatabase) = database.deckDao()
    @Provides
    fun provideReviewCardsDao(database: AppDatabase) = database.reviewCardsDao()

    // Add more as needed
    @Provides
    @Singleton
    fun provideReviewCardRepository(
        reviewCardsDao: ReviewCardsDao
    ): ReviewCardRepository {
        return ReviewCardRepository(reviewCardsDao)
    }

    @Provides
    @Singleton
    fun provideDeckRepository(
        deckDao: DeckDao
    ) : DeckRepository {
        return DeckRepository(deckDao)
    }
}