package com.example.studyio.data

import com.example.studyio.data.entities.StudyioDatabase
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.Note
import com.example.studyio.data.entities.Card
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream
import android.database.sqlite.SQLiteDatabase

suspend fun importAnkiApkgFromStream(
    inputStream: InputStream,
    db: StudyioDatabase,
    tempDir: File
) {
    val zipInput = ZipInputStream(inputStream)
    var collectionFile: File? = null
    while (true) {
        val entry = zipInput.nextEntry ?: break
        if (entry.name == "collection.anki2") {
            collectionFile = File.createTempFile("collection", ".anki2", tempDir)
            collectionFile.outputStream().use { zipInput.copyTo(it) }
        }
        zipInput.closeEntry()
    }
    zipInput.close()
    if (collectionFile == null) return

    // 2. Open SQLite
    val ankiDb = SQLiteDatabase.openDatabase(collectionFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

    // 3. Read col table for decks
    val colCursor = ankiDb.rawQuery("SELECT * FROM col", null)
    if (colCursor.moveToFirst()) {
        val decksJson = colCursor.getString(colCursor.getColumnIndexOrThrow("decks"))
        val decksObj = JSONObject(decksJson)
        for (key in decksObj.keys()) {
            val deckObj = decksObj.getJSONObject(key)
            val id = deckObj.getLong("id")
            val name = deckObj.getString("name")
            val desc = if (deckObj.has("desc")) deckObj.getString("desc") else null
            db.deckDao().insertDeck(
                Deck(
                    id = id,
                    name = name,
                    description = desc
                )
            )
        }
    }
    colCursor.close()

    // 4. Read notes
    val notesCursor = ankiDb.rawQuery("SELECT id, mid, flds, tags, guid FROM notes", null)
    while (notesCursor.moveToNext()) {
        val id = notesCursor.getLong(0)
        val modelId = notesCursor.getLong(1)
        val fields = notesCursor.getString(2)
        val tags = notesCursor.getString(3)
        val guid = notesCursor.getString(4)
        val note = Note(
            id = id,
            modelId = modelId,
            fields = fields,
            tags = tags,
            guid = guid
        )
        db.noteDao().insertNote(note)
    }
    notesCursor.close()

    // 5. Read cards
    val cardsCursor = ankiDb.rawQuery("SELECT id, nid, did, ord, type, queue, due, ivl, reps, lapses FROM cards", null)
    while (cardsCursor.moveToNext()) {
        val deckId = cardsCursor.getLong(2)
        val noteId = cardsCursor.getLong(1)
        val card = Card(
            deckId = deckId,
            noteId = noteId,
            ord = cardsCursor.getInt(3),
            type = cardsCursor.getInt(4),
            queue = cardsCursor.getInt(5),
            due = cardsCursor.getInt(6),
            interval = cardsCursor.getInt(7),
            reps = cardsCursor.getInt(8),
            lapses = cardsCursor.getInt(9)
        )
        db.cardDao().insertCard(card)
    }
    cardsCursor.close()
    ankiDb.close()
    collectionFile.delete()
} 