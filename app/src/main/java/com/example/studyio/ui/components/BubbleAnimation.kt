package com.example.studyio.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.*

@Composable
fun BubbleAnimation() {
    val bubblesComposition by rememberLottieComposition(LottieCompositionSpec.Asset("floating.json"))
    val bubblesProgress by animateLottieCompositionAsState(
        bubblesComposition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = bubblesComposition,
        progress = bubblesProgress,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}
