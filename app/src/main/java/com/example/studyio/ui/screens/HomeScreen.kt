package com.example.studyio.ui.screens

import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.studyio.R
import androidx.navigation.NavController
import com.example.studyio.ui.components.SignUp
import com.example.studyio.ui.components.Login
import com.example.studyio.ui.components.GradientButton
import com.example.studyio.ui.components.AnimatedBackground

@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background as bottom layer
        AnimatedBackground()

        // Foreground content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(300.dp)
                    .padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GradientButton(
                    text = "Login",
                    onClick = { navController.navigate("loggedin") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                GradientButton(
                    text = "Signup",
                    onClick = { navController.navigate("signup") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

