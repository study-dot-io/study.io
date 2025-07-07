package com.example.studyio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studyio.viewmodel.getClasses
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@Composable
fun ClassList(navController: NavController) {
    val classList = getClasses()
    val scrollState = rememberScrollState()

    var containerWidthPx by remember { mutableStateOf(0) }
    var contentWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    var scrollbarVisible by remember { mutableStateOf(false) }

    val scrollbarAlpha by animateFloatAsState(targetValue = if (scrollbarVisible) 1f else 0f)

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            scrollbarVisible = true
        } else {
            delay(1000)
            scrollbarVisible = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* navController.navigate("courses") */ },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Courses",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Go to Courses",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // height for buttons + scrollbar
            .onSizeChanged { containerWidthPx = it.width }
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .onSizeChanged { contentWidthPx = it.width }
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            classList.forEach { className ->
                Button(
                    onClick = { navController.navigate("selectedClassScreen/$className") },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                ) {
                    Text(text = className, fontSize = 12.sp)
                }
            }
        }

        if (contentWidthPx > containerWidthPx && scrollbarAlpha > 0f) {
            val scrollbarHeight = 4.dp
            val scrollbarPadding = 16.dp

            val containerWidth = containerWidthPx.toFloat()
            val contentWidth = contentWidthPx.toFloat()

            val visibleFraction = containerWidth / contentWidth
            val minThumbWidthPx = with(density) { 20.dp.toPx() }
            val thumbWidthPx = max(containerWidth * visibleFraction, minThumbWidthPx)

            val scrollOffset = scrollState.value.toFloat()
            val maxScrollOffset = scrollState.maxValue.toFloat()

            val maxThumbOffset = containerWidth - thumbWidthPx
            val thumbOffsetPx = min(scrollOffset / maxScrollOffset * maxThumbOffset, maxThumbOffset)

            Box(
                modifier = Modifier
                    .height(scrollbarHeight)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = scrollbarPadding)
                    .graphicsLayer(alpha = scrollbarAlpha) // fade scrollbar in/out
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = with(density) { thumbOffsetPx.toDp() })
                        .width(with(density) { thumbWidthPx.toDp() })
                        .height(scrollbarHeight)
                        .background(Color.Gray.copy(alpha = 0.7f), shape = CircleShape)
                )
            }
        }
    }
}
