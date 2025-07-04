package com.example.studyio.data.db
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.studyio.data.db.DAO.DeckDao
import com.example.studyio.data.db.DAO.ReviewCardsDao
import com.example.studyio.data.model.Deck
import com.example.studyio.data.model.ReviewCards

@Database(entities = [Deck::class, ReviewCards::class], version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun reviewCardsDao(): ReviewCardsDao

}