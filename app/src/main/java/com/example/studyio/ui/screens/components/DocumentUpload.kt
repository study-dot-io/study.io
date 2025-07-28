package com.example.studyio.ui.screens.components

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyio.data.entities.Deck
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.screens.CardCreateViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.util.concurrent.TimeUnit

@Composable
fun GetDocumentFromUser(user: FirebaseUser? = null) {
    Log.d("GetDocument", "Entered the get document function")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf<String?>(null) }
    var filePath by remember { mutableStateOf<Uri?>(null) }
    var llmResult by remember { mutableStateOf<Map<String, Any>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        filePath = uri
        fileName = uri?.lastPathSegment
    }

    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        Button(onClick = { launcher.launch("*/*") }) {
            Text("Upload Document")
        }

        Spacer(modifier = Modifier.height(10.dp))

        fileName?.let {
            Text(text = "Selected File: $it")
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (filePath != null && user != null) {
                    Log.d("GetDocument", "Should call the llm function")
                    scope.launch {
                        llmResult = uploadDocumentToLLM(user, context, filePath!!)
                    }
                }
            },
            enabled = filePath != null
        ){
            Text("Upload")
        }
        
        llmResult?.let { CreateLLMDeckAndCard(it) }
    }
}

suspend fun uploadDocumentToLLM(
    user: FirebaseUser,
    context: Context,
    filePath: Uri
): Map<String, Any>? {
    val idTokenResult = user.getIdToken(true).await()
    val idToken = idTokenResult.token
    if (idToken == null) {
        Log.e("UploadDocument", "Failed to get ID token")
        return null
    }

    Log.d("UploadDocument", "user name: ${user.displayName}")
    Log.d("UploadDocument", "id token: $idToken")
    val inputStream = context.contentResolver.openInputStream(filePath) ?: return null
    val bytes = inputStream.readBytes()
    inputStream.close()
    val base64File = Base64.encodeToString(bytes, Base64.NO_WRAP)
    val fileName = filePath.lastPathSegment ?: "document.pdf"

    Log.d("UploadDocument", "file name: $fileName")
    Log.d("UploadDocument", "bytes length: ${bytes.size}")
    val jsonRequest = """
        {
            "login_token": "$idToken",
            "file_name": "$fileName",
            "file": "$base64File"
        }
    """.trimIndent()

    Log.d("UploadDocument", "request body: $jsonRequest")

    val requestBody = jsonRequest.toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.20.10.2:5001/generate_flashcards")
        .post(requestBody)
        .addHeader("Content-Type", "application/json")
        .build()

    val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    val result = mutableMapOf<String, Any>()

    withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("Upload", "Failed: ${response.code}")
            } else {
                val responseBody = response.body?.string()
                Log.d("Upload", "Success: $responseBody")

                val jsonArray = JSONArray(responseBody)
                val jsonObj = jsonArray.getJSONObject(0)
                val jsonCardsArray: JSONArray = jsonObj.getJSONArray("cards")
                val jsonDeckObj = jsonObj.getJSONObject("deck")
                val deckMap = mutableMapOf<String, Any>()
                deckMap["id"] = jsonDeckObj.getString("id")
                deckMap["name"] = jsonDeckObj.getString("name")
                deckMap["createdAt"] = jsonDeckObj.getString("createdAt")
                result["deck"] = deckMap

                val cardsList = mutableListOf<Map<String, Any>>()

                for (i in 0 until jsonCardsArray.length()) {
                    val cardObj = jsonCardsArray.getJSONObject(i)
                    val cardMap = mutableMapOf<String, Any>()
                    cardMap["front"] = cardObj.getString("front")
                    cardMap["back"] = cardObj.getString("back")
                    cardsList.add(cardMap)
                }
                result["cards"] = cardsList
            }
        }
    }

    return result
}

@Composable
fun CreateLLMDeckAndCard(llmResponse: Map<String, Any>) {
    Log.d("CreateLLMDeckAndCard", "Entered the CreateLLMDeckAndCard function")
    val homeViewModel: HomeViewModel = hiltViewModel()
    Log.d("CreateLLMDeckAndCard", "LLm response: $llmResponse")
    
    // Safely cast the deck object with proper type checking
    val deckAny = llmResponse["deck"] ?: return
    if (deckAny !is Map<*, *>) {
        Log.e("CreateLLMDeckAndCard", "Deck object is not a Map")
        return
    }
    
    val deckObj = deckAny.entries.associate { (key, value) -> 
        (key as? String ?: key.toString()) to value 
    }
    
    val deckId = deckObj["id"]?.toString() ?: return
    Log.d("CreateLLMDeckAndCard", "LLm response deck ID: $deckId")
    
    val newDeck = Deck(
        id = deckId,
        name = deckObj["name"]?.toString() ?: "",
        description = "",
        color = "#6366F1"
    )
    homeViewModel.createDeck(newDeck)
    
    // Safely cast the cards list with proper type checking
    val cardsAny = llmResponse["cards"] ?: return
    if (cardsAny !is List<*>) {
        Log.e("CreateLLMDeckAndCard", "Cards object is not a List")
        return
    }
    
    val cardCreateViewModel: CardCreateViewModel = hiltViewModel()
    
    // Process each card with safe type checking
    for (cardAny in cardsAny) {
        if (cardAny !is Map<*, *>) {
            Log.e("CreateLLMDeckAndCard", "Card is not a Map")
            continue
        }
        
        // Safely extract front and back values
        val front = cardAny["front"]?.toString() ?: ""
        val back = cardAny["back"]?.toString() ?: ""
        val tags = ""
        
        cardCreateViewModel.createCard(deckId = deckId, front = front, back = back, tags = tags, onDone = {})
    }
    
    Log.d("CreateLLMDeckAndCard", "Created the cards")
}
