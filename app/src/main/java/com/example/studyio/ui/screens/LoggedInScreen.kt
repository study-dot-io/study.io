package com.example.studyio.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.studyio.ui.components.*
import androidx.compose.ui.zIndex
import com.example.studyio.R

@Composable
fun LoggedInScreen() {
    val internalNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = internalNavController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_mix),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            NavHost(
                navController = internalNavController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .zIndex(1f)
            ) {
                composable("home") {
                    HomeScreenContent(navController = internalNavController)
                }
                composable("social") {
                    Social()
                }
                composable("profile") {
                    Profile()
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Good morning, Priyal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { /* navController.navigate("courses") */ },
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Courses",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold
//            )
//            Icon(
//                imageVector = Icons.Default.ArrowForward,
//                contentDescription = "Go to Courses",
//                tint = MaterialTheme.colorScheme.primary
//            )
//        }

        ClassList(navController = navController)
        ReviewsDueSection()
        GenerateNotesSection()
    }
}
