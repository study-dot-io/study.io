package com.example.studyio.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.studyio.data.entities.Deck
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.auth.AuthViewModel
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.platform.LocalContext
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.tasks.await
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    decks: List<Deck>,
    dueCards: Int,
    todayReviews: Int,
    totalCards: Int,
    totalDecks: Int,
    isImporting: Boolean = false,
    importMessage: String = "",
    onDeckClick: (Deck) -> Unit = {},
    onCreateDeck: () -> Unit = {},
    onStudyNow: () -> Unit = {},
    onImportApkg: (() -> Unit)? = null,
    onStudyNowForDeck: (Deck) -> Unit = {},
    onDeleteDeck: (Deck) -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onSignOut: (() -> Unit)? = null,
) {
//    var deckToDelete by remember { mutableStateOf<Deck?>(null) }
    var selectedDeck by remember { mutableStateOf<Deck?>(null) }
    val homeViewModel: HomeViewModel = hiltViewModel()
    var showUserInfo by remember { mutableStateOf(false) }
    var shareDeckEmailPrompt by remember { mutableStateOf(false) }
    var emailToShare by remember { mutableStateOf("") }
    var selectedDeckForShare by remember { mutableStateOf<Deck?>(null) }
    val context = LocalContext.current
    
    val authViewModel: AuthViewModel = hiltViewModel()
    val user by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "StudyIO",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (user == null) {
                            onNavigateToAuth()
                        } else {
                            showUserInfo = true
                        }
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "User Info")
                    }

                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            var fabExpanded by remember { mutableStateOf(false) }
            Box {
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Actions")
                }
                DropdownMenu(
                    expanded = fabExpanded,
                    onDismissRequest = { fabExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Create Deck") },
                        onClick = {
                            fabExpanded = false
                            onCreateDeck()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import Anki Deck (.apkg)") },
                        onClick = {
                            fabExpanded = false
                            if (onImportApkg != null) onImportApkg()
                        },
                        enabled = !isImporting
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Button(
                onClick = {
                    if (user == null) {
                        onNavigateToAuth()
                    } else {
                        showUserInfo = true
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (user == null) "Test Auth" else "Show User Info")
            }
            if (user != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "👋 Welcome, ${user?.displayName ?: user?.email ?: "User"}!",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            authViewModel.signOut()
                            if (onSignOut != null) onSignOut()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Sign Out")
                    }
                }
            }
            if (showUserInfo && user != null) {
                AlertDialog(
                    onDismissRequest = { showUserInfo = false },
                    title = { Text("Signed In") },
                    text = {
                        Text("You are signed in as: ${user?.displayName ?: user?.email ?: user?.uid}")
                    },
                    confirmButton = {
                        TextButton(onClick = { showUserInfo = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    StudyNowCard(
                        dueCards = dueCards,
                        todayReviews = todayReviews,
                        onStudyNow = onStudyNow
                    )
                }

                item {
                    QuickStatsCard(
                        totalDecks = totalDecks,
                        totalCards = totalCards
                    )
                }

                // Import status card
                if (isImporting) {
                    item {
                        ImportStatusCard(
                            message = importMessage
                        )
                    }
                }

                item {
                    Text(
                        text = "Your Decks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(decks) { deck ->
                    DeckCard(
                        deck = deck,
                        onClick = { onDeckClick(deck) },
                        onReview = { onStudyNowForDeck(deck) },
                        onLongPress = { selectedDeck = deck }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
                item{
                    GetDocumentFromUser(user)
                }
            }
        }

        // Loading overlay dialog
        if (isImporting) {
            Dialog(
                onDismissRequest = { /* Prevent dismissal during import */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                ImportLoadingDialog(message = importMessage)
            }
        }

        // Delete deck confirmation dialog
        selectedDeck?.let { deck ->
            AlertDialog(
                onDismissRequest = { selectedDeck = null },
                title = { Text("Update Deck") },
                text = { Text("Update the deck '${deck.name}'?") },
                confirmButton = {
                    Column {
                        TextButton(onClick = {
                            val updatedDeck = deck.copy(isPublic = !deck.isPublic)
                            homeViewModel.updateDeck(updatedDeck)
                            selectedDeck = null
                        }) {
                            Text(if (deck.isPublic) "Make Private" else "Make Public")
                        }
                        TextButton(onClick = {
                            if (user == null) {
                                Toast.makeText(context, "You must be signed in to share a deck.", Toast.LENGTH_SHORT).show()
                                onNavigateToAuth()
                            } else if (!deck.isPublic) {
                                Toast.makeText(context, "The deck must be public to be shared.", Toast.LENGTH_SHORT).show()
                            }else {
                                selectedDeckForShare = deck
                                shareDeckEmailPrompt = true  // Show email prompt
                                selectedDeck = null
                            }
                        }) {
                            Text("Share")
                        }
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            onDeleteDeck(deck)
                            selectedDeck = null
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { selectedDeck = null }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
        if (shareDeckEmailPrompt && selectedDeckForShare != null) {
            AlertDialog(
                onDismissRequest = { shareDeckEmailPrompt = false },
                title = { Text("Share Deck") },
                text = {
                    OutlinedTextField(
                        value = emailToShare,
                        onValueChange = { emailToShare = it },
                        label = { Text("Enter recipient email") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        sendDeckByEmail(context, selectedDeckForShare!!, emailToShare)
                        shareDeckEmailPrompt = false
                        emailToShare = ""
                    }) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        shareDeckEmailPrompt = false
                        emailToShare = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

}

@Composable
fun ImportLoadingDialog(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Importing Anki Deck",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImportStatusCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.tertiary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Importing...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StudyNowCard(
    dueCards: Int,
    todayReviews: Int,
    onStudyNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Study Now",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$dueCards cards due",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Button(
                    onClick = onStudyNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.AccessTime,
                    value = todayReviews.toString(),
                    label = "Today's Reviews"
                )
                StatItem(
                    icon = Icons.Default.Star,
                    value = "85%",
                    label = "Success Rate"
                )
            }
        }
    }
}

@Composable
fun QuickStatsCard(
    totalDecks: Int,
    totalCards: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Folder,
                value = totalDecks.toString(),
                label = "Decks"
            )
            StatItem(
                icon = Icons.Default.Star,
                value = totalCards.toString(),
                label = "Total Cards"
            )
            StatItem(
                icon = Icons.Default.AccessTime,
                value = "12h",
                label = "Study Time"
            )
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DeckCard(
    deck: Deck,
    onClick: () -> Unit,
    onReview: () -> Unit,
    onLongPress: () -> Unit // Replace onDelete with onLongPress
) {
    val viewModel: HomeViewModel = hiltViewModel()
    
    val cardCountMap by viewModel.cardCountMap.collectAsState()
    // check keys in the deck count map
    Log.d("DeckCard", "Deck Count Map: ${cardCountMap.keys.joinToString(", ")}")

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(deck.color.toColorInt()))
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Deck info (clickable for deck details)
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
//                Text(
//                    text = deck.name,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (deck.isPublic) "(Public)" else "(Private)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                deck.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Due Cards: ${cardCountMap[deck.id] ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onReview, enabled = (cardCountMap[deck.id] ?: 0) > 0) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Review")
            }
        }
    }
}

// Upload to LLM feature
@Composable
fun GetDocumentFromUser(user: FirebaseUser? = null) {
    // GEt context -> required for all file ops
    Log.d("GetDocument", "Entered the get document function")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fileName by remember {mutableStateOf<String?>(null)}
    var filePath by remember {mutableStateOf<Uri?>(null)}
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
                                llmResult = UploadDocumentToLLM(user,context, filePath!!)
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

suspend fun UploadDocumentToLLM(
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
fun CreateLLMDeckAndCard(llmResponse:Map<String, Any>) {
    Log.d("CreateLLMDeckAndCard", "Entered the CreateLLMDeckAndCard function")
    val homeViewModel: HomeViewModel = hiltViewModel()
    Log.d("CreateLLMDeckAndCard", "LLm response: ${llmResponse}")
    val deckObj = llmResponse["deck"] as? Map<String, Any> ?: return
    val deckId = deckObj["id"]?.toString() ?: return
    Log.d("CreateLLMDeckAndCard", "LLm response: ${deckId}")
    val newDeck = Deck(
        id = deckId,
        name = deckObj["name"]?.toString() ?: "",
        description = "",
        color = "#6366F1"
    )
    homeViewModel.createDeck(newDeck)
    val cardList = llmResponse["cards"] as? List<Map<String, Any>> ?: return
    val cardCreateViewModel: CardCreateViewModel = hiltViewModel()
    for (i in 0 until cardList.size) {
        val cardObj = cardList[i]
        val front = cardObj["front"] as? String ?: ""
        val back = cardObj["back"] as? String ?: ""
        val tags = ""
        cardCreateViewModel.createCard(deckId = deckId, front = front, back= back, tags = tags, onDone = {})

    }
    Log.d("CreateLLMDeckAndCard", "Created the cards")

}