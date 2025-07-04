package com.example.studyio

import com.example.studyio.data.importAnkiApkgFromStream
import com.example.studyio.data.entities.StudyioDatabase
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun testAnkiApkgImport() {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudyioDatabase::class.java
        ).build()
        val resource = this::class.java.classLoader?.getResource("test.apkg")
        require(resource != null) { "test.apkg not found in resources" }
        val testApkgFile = File(resource.file)
        require(testApkgFile.exists()) { "test.apkg not found: ${testApkgFile.absolutePath}" }
        runBlocking {
            importAnkiApkgFromStream(testApkgFile.inputStream(), db, testApkgFile.parentFile ?: File("/tmp"))
        }
        
        // Assert decks, notes, and cards are imported
        val testDeck =
            runBlocking { db.deckDao().getAllDecks() }.first { deck -> deck.name.contains("Most Common English Words") }
        val notes = runBlocking { db.noteDao().getNotesForDeck(testDeck.id) }
        val cards = runBlocking { db.cardDao().getCardsForDeck(testDeck.id) }
        assertTrue(notes.isNotEmpty())
        assertTrue(cards.isNotEmpty())
    }
}