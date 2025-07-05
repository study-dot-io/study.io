package com.example.studyio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF004AAD), Color(0xFFCB6CE6))
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
