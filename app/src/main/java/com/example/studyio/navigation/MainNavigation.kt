package com.example.studyio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.studyio.ui.components.Login
import com.example.studyio.ui.components.SignUp
import com.example.studyio.ui.screens.HomeScreen
import com.example.studyio.ui.screens.LoggedInScreen
import com.example.studyio.ui.screens.SelectedClassScreen
@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {HomeScreen(navController)}
        composable("login") {Login()}
        composable("signup") { SignUp() }
        composable("loggedin"){LoggedInScreen(navController)}
        composable("selectedClassScreen/{classId}"){backStackEntry ->
            // backStackEntry is a lambda thats a part of the NavController
            // It's used for accessing args passed in during nav
            // ? is used for null checks
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            SelectedClassScreen(classId=classId, navController=navController)
        }
    }
}