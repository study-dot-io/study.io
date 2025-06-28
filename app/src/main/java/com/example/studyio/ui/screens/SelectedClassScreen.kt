package com.example.studyio.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material3.Text

@Composable
fun SelectedClassScreen(
    classId: String,
    navController: NavController

){
    Text(classId)
}