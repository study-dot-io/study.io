package com.example.studyio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.studyio.ui.components.Login
import com.example.studyio.ui.components.SignUp
import com.example.studyio.ui.screens.HomeScreen
import com.example.studyio.ui.screens.LoggedInScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {HomeScreen(navController)}
        composable("login") {Login()}
        composable("signup") { SignUp() }
        composable("loggedin"){LoggedInScreen(navController)}
    }
}