package com.example.studyio.data

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardType
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.StudyioDatabase
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

private const val TAG = "StudyIO-AnkiImporter"

suspend fun importAnkiApkgFromStream(
    inputStream: InputStream,
    db: StudyioDatabase,
    tempDir: File,
    onProgress: (String) -> Unit = {}
) {
    Log.i(TAG, "Starting APKG import process")
    onProgress("Extracting APKG file...")

    // 1. Extract collection.anki2 from the ZIP
    Log.d(TAG, "Extracting ZIP file...")
    val zipInput = ZipInputStream(inputStream)
    var collectionFile: File? = null
    var entryCount = 0

    while (true) {
        val entry = zipInput.nextEntry ?: break
        entryCount++
        Log.d(TAG, "Found ZIP entry: ${entry.name}")

        if (entry.name == "collection.anki2") {
            Log.d(TAG, "Found collection.anki2 file")
            collectionFile = File.createTempFile("collection", ".anki2", tempDir)
            collectionFile.outputStream().use { zipInput.copyTo(it) }
            Log.d(TAG, "Extracted collection.anki2 to: ${collectionFile.absolutePath}")
        }
        zipInput.closeEntry()
    }
    zipInput.close()

    Log.i(TAG, "Processed $entryCount entries from ZIP file")

    if (collectionFile == null) {
        Log.e(TAG, "No collection.anki2 file found in APKG")
        throw Exception("Invalid APKG file: collection.anki2 not found")
    }

    onProgress("Reading Anki database...")
    Log.d(TAG, "Opening SQLite database: ${collectionFile.absolutePath}")

    // 2. Open SQLite database
    val ankiDb = SQLiteDatabase.openDatabase(collectionFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

    try {
        // 3. Read decks from col table
        Log.d(TAG, "Reading decks from col table...")
        onProgress("Importing decks...")

        val colCursor = ankiDb.rawQuery("SELECT * FROM col", null)
        var deckCount = 0
        val ankiDeckIdToOurDeckId = mutableMapOf<Long, String>()

        if (colCursor.moveToFirst()) {
            val decksJson = colCursor.getString(colCursor.getColumnIndexOrThrow("decks"))
            Log.d(TAG, "Decks JSON: $decksJson")

            val decksObj = JSONObject(decksJson)
            for (key in decksObj.keys()) {
                val deckObj = decksObj.getJSONObject(key)
                val name = deckObj.getString("name")
                val desc = if (deckObj.has("desc")) deckObj.getString("desc") else null
                val id = deckObj.getLong("id")

                // Skip default deck (ID: 1) if it's empty or has generic name
                if (id == 1L && name == "Default") {
                    Log.d(TAG, "Skipping default deck")
                    continue
                }

                val ourDeckId = java.util.UUID.randomUUID().toString()
                try {
                    db.deckDao().insertDeck(
                        Deck(
                            id = ourDeckId,
                            name = name,
                            description = desc
                        )
                    )
                    ankiDeckIdToOurDeckId[id] = ourDeckId
                    deckCount++
                    Log.d(TAG, "Successfully inserted deck: $name")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to insert deck $name: ${e.message}")
                }
            }
        }
        colCursor.close()

        Log.i(TAG, "Imported $deckCount decks")
        onProgress("Importing notes...")

        // 4. Read notes and build a map for merging
        Log.d(TAG, "Reading notes...")
        val notesCursor = ankiDb.rawQuery("SELECT id, flds, tags FROM notes", null)
        val noteMap = mutableMapOf<Long, Pair<String, String>>()
        while (notesCursor.moveToNext()) {
            val id = notesCursor.getLong(0)
            val fields = notesCursor.getString(1)
            val tags = notesCursor.getString(2)
            noteMap[id] = Pair(fields, tags)
        }
        notesCursor.close()
        Log.i(TAG, "Loaded ${noteMap.size} notes for merging")
        onProgress("Importing cards...")

        // 5. Read cards and merge note data
        Log.d(TAG, "Reading cards...")
        val cardsCursor = ankiDb.rawQuery("SELECT id, nid, did, type, due FROM cards", null)
        var cardCount = 0
        while (cardsCursor.moveToNext()) {
            val cardId = cardsCursor.getLong(0)
            val noteId = cardsCursor.getLong(1)
            val deckId = cardsCursor.getLong(2)
            cardsCursor.getInt(3)
            val due = cardsCursor.getLong(4)

            val noteData = noteMap[noteId]
            if (noteData == null) {
                Log.w(TAG, "No note found for card $cardId (noteId: $noteId), skipping")
                continue
            }
            val (fields, tags) = noteData

            val ourDeckId = ankiDeckIdToOurDeckId[deckId] ?: deckId.toString()
            val card = Card(
                id = java.util.UUID.randomUUID().toString(),
                deckId = ourDeckId,
                type = CardType.NEW,
                due = due,
                front = fields.split("\u001F").getOrNull(0) ?: "",
                back = fields.split("\u001F").drop(1).joinToString("\n"),
                tags = tags
            )

            try {
                db.cardDao().insertCard(card)
                cardCount++
                if (cardCount % 100 == 0) {
                    Log.d(TAG, "Imported $cardCount cards so far...")
                    onProgress("Importing cards... ($cardCount)")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to insert card ID $cardId: ${e.message}")
            }
        }
        cardsCursor.close()
        Log.i(TAG, "Imported $cardCount cards")
        onProgress("Finalizing import...")

        // Final summary
        Log.i(TAG, "Import completed successfully!")

    } catch (e: Exception) {
        Log.e(TAG, "Error during import process", e)
        throw e
    } finally {
        // 6. Cleanup
        Log.d(TAG, "Cleaning up resources...")
        ankiDb.close()
        val deleted = collectionFile.delete()
        Log.d(TAG, "Temporary file deleted: $deleted")
    }

    Log.i(TAG, "APKG import process completed successfully")
}