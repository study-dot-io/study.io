package com.example.studyio.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyio.ui.home.HomeViewModel
import com.example.studyio.ui.importer.ImportViewModel
import com.example.studyio.ui.screens.AuthScreen
import com.example.studyio.ui.screens.CardCreateScreen
import com.example.studyio.ui.screens.CreateDeckScreen
import com.example.studyio.ui.screens.DeckDetailScreen
import com.example.studyio.ui.screens.HomeScreen
import com.example.studyio.ui.screens.QuizScreen
import com.example.studyio.ui.demo.ApiDemoScreen
import com.example.studyio.ui.screens.AnalyticsScreen
import com.example.studyio.ui.screens.SocialScreen

import com.example.studyio.ui.screens.BottomNavBar
import com.example.studyio.ui.screens.bottomNavItems

@Composable
fun StudyIONavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val importViewModel: ImportViewModel = hiltViewModel()
    val isImporting by importViewModel.isImporting.collectAsState()
    val importMessage by importViewModel.importMessage.collectAsState()

    // Launcher for importing Anki APKG files
    val importApkgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                importViewModel.importApkg(
                    context = context,
                    uri = uri,
                    onComplete = { success, _ ->
                        if (success) homeViewModel.loadDecks()
                    }
                )
            }
        }
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // Apply window insets padding to extend behind navigation bar
                Box(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    BottomNavBar(navController)
                }
            }
        },
        // Set contentWindowInsets to empty to manually handle insets
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    isImporting = isImporting,
                    importMessage = importMessage,
                    onDeckClick = { deck -> navController.navigate("decks/${deck.id}") },
                    onCreateDeck = { navController.navigate("decks/create") },
                    onImportApkg = { importApkgLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                    onStudyNowForDeck = { deck -> navController.navigate("quiz/decks/${deck.id}") },
                    onDeleteDeck = { deck -> homeViewModel.deleteDeck(deck.id) },
                    onNavigateToAuth = { navController.navigate("auth") }
                )
            }

            composable("decks/create") {
                CreateDeckScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onDeckCreated = { newDeck ->
                        homeViewModel.createDeck(newDeck) { navController.popBackStack() }
                    }
                )
            }

            composable("decks/{id}") { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("id") ?: return@composable
                DeckDetailScreen(
                    deckId = deckId,
                    onBack = { navController.popBackStack() },
                    onCreateCardPressed = { navController.navigate("decks/$deckId/createCard") }
                )
            }

            composable("decks/{deckId}/createCard") { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
                CardCreateScreen(
                    deckId = deckId,
                    onDeckSelected = { newDeckId ->
                        navController.popBackStack("decks/$deckId", inclusive = true)
                        navController.navigate("decks/$newDeckId")
                        navController.navigate("decks/$newDeckId/createCard")
                    },
                    onBackPressed = { navController.popBackStack() },
                    onCreatePressed = { navController.popBackStack() }
                )
            }

            composable("quiz/decks/{deckId}") { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
                QuizScreen(
                    deckId = deckId,
                    onQuizComplete = { navController.popBackStack("home", inclusive = false) }
                )
            }

            composable("auth") {
                AuthScreen(
                    onAuthSuccess = { navController.popBackStack() }
                )
            }

            composable("apiDemo") {
                ApiDemoScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("social") {
                SocialScreen()
            }

            composable("analytics") {
                AnalyticsScreen(
                    onDeckSelected = { deckId: String ->
                        navController.navigate("decks/$deckId")
                    }
                )
            }
        }
    }
}

