package com.example.studyio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studyio.ui.components.FriendActivity
import com.example.studyio.ui.components.FriendActivityList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen() {
    val sampleFriends = listOf(
        FriendActivity("JK", "Jerry King", listOf("10 cards created", "3 courses reviewed")),
        FriendActivity("TS", "Thomas Smith", listOf("10 cards created")),
        FriendActivity("BJ", "Ben Jerry", listOf("3 courses reviewed")),
        FriendActivity("LS", "Lisa Smith", listOf("10 cards created", "3 courses reviewed")),
        FriendActivity("MT", "Mathew Thompson", listOf("10 cards created", "3 courses reviewed")),
        FriendActivity("AB", "Alex Brown", listOf("7 cards created")),
        FriendActivity("CD", "Cathy Doe", listOf("1 course reviewed")),
        FriendActivity("EF", "Elliot Fox", listOf("12 cards created")),
        FriendActivity("GH", "Grace Hall", listOf("2 courses reviewed")),
        FriendActivity("IJ", "Isaac Jones", listOf("5 cards created", "1 course reviewed"))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Social",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Activity of your Friends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            FriendActivityList(
                friends = sampleFriends,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

//working function to retrieve friends list from end point from backend service
//
//suspend fun getFriends(
//    user: FirebaseUser,
//    context: Context
//): List<String> {
//    val idTokenResult = user.getIdToken(true).await()
//    val idToken = idTokenResult.token ?: return emptyList()
//
//    val jsonRequest = """
//        {
//            "login_token": "$idToken"
//        }
//    """.trimIndent()
//
//    val requestBody = jsonRequest.toRequestBody("application/json".toMediaTypeOrNull())
//
//    val request = Request.Builder()
//        .url("http://172.20.10.2:5001/get_friends")
//        .post(requestBody)
//        .addHeader("Content-Type", "application/json")
//        .build()
//
//    val client = OkHttpClient.Builder()
//        .connectTimeout(120, TimeUnit.SECONDS)
//        .writeTimeout(120, TimeUnit.SECONDS)
//        .readTimeout(120, TimeUnit.SECONDS)
//        .build()
//
//    return withContext(Dispatchers.IO) {
//        val response = client.newCall(request).execute()
//        if (!response.isSuccessful) {
//            Log.e("getFriends", "Request failed: ${response.code}")
//            return@withContext emptyList()
//        }
//
//        val responseBody = response.body?.string() ?: return@withContext emptyList()
//        Log.d("getFriends", "Success: $responseBody")
//
//        val jsonArray = JSONArray(responseBody)
//        val firstObject = jsonArray.getJSONObject(0)
//        val friendsJsonArray = firstObject.getJSONArray("friends")
//
//        val names = mutableListOf<String>()
//        for (i in 0 until friendsJsonArray.length()) {
//            val friend = friendsJsonArray.getJSONObject(i)
//            names.add(friend.getString("name"))
//        }
//        return@withContext names
//    }
//}
//
