package com.example.studyio.ui.screens

import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.studyio.ui.components.SignUp
import com.example.studyio.ui.components.Login

@Composable
fun HomeScreen(navController: NavController)  {
     Column(modifier = Modifier
         .padding(top = 100.dp, bottom = 16.dp)
         .fillMaxWidth()
     ){
         Row(
             modifier = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.Center
         ) {
             Text(text = "study.io", fontSize = 90.sp, color = Color.Blue)
         }
         Spacer(modifier = Modifier.height(80.dp))

         // Add logo or something
         Text(text="Insert logo",  fontSize = 40.sp)

         Spacer(modifier = Modifier.height(40.dp))

         Row(
             modifier = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.SpaceBetween
         ) {
             Column(modifier = Modifier.weight(1f)) {
                 // For the demo I am redirecting it to the LoggedIn component
                 Button(onClick = {navController.navigate("loggedin")}) {
                     Text(text="Login", fontSize=40.sp)
                 }
             }

             Column(modifier = Modifier.weight(1f)) {
                 Button(onClick = {navController.navigate("signup")}) {
                     Text(text="Signup", fontSize=40.sp)
                 }
             }
         }
     }
}

