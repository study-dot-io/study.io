package com.example.studyio.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Default.Home, "Home"),
    BottomNavItem("social", Icons.Default.Group, "Social"),
    BottomNavItem("analytics", Icons.Default.Person, "Analytics"),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("home") { inclusive = false }
//                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
//                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}