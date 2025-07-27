package com.example.studyio.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.studyio.data.entities.Deck

fun sendDeckByEmail(context: Context, deck: Deck, email: String) {
    val subject = "Shared Deck: ${deck.name}"
    val body = "Deck Name: ${deck.name}\nDescription: ${deck.description ?: "No description"}"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    } catch (ex: android.content.ActivityNotFoundException) {
        Log.e("ShareDeck", "No email clients installed.")
    }
}
