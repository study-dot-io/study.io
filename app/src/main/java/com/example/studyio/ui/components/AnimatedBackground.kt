package com.example.studyio.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.*

@Composable
fun AnimatedBackground() {
    val bgComposition by rememberLottieComposition(LottieCompositionSpec.Asset("bg.json"))
    val bgProgress by animateLottieCompositionAsState(bgComposition, iterations = LottieConstants.IterateForever)

    val bubblesComposition by rememberLottieComposition(LottieCompositionSpec.Asset("floating.json"))
    val bubblesProgress by animateLottieCompositionAsState(bubblesComposition, iterations = LottieConstants.IterateForever)

    Box(modifier = Modifier.fillMaxSize()) {
        if (bgComposition != null) {
            LottieAnimation(
                composition = bgComposition,
                progress = bgProgress,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // <- key part
            )
        }

        if (bubblesComposition != null) {
            LottieAnimation(
                composition = bubblesComposition,
                progress = bubblesProgress,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}
