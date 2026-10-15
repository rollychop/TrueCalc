package com.brohit.truecalc.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brohit.truecalc.ui.theme.Spacing
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

private val AnimationSize = 200.dp

@Composable
fun SearchLoadingView(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_m6cuL6.json")
    )
    
    val tips = remember {
        listOf(
            "Tip: You can search for BMI to check your health!",
            "Tip: Use EMI calculator to plan your loans better.",
            "Tip: Search for Currency to convert between different rates.",
            "Tip: Tap on 'Past Calculations' to restore your previous work.",
            "Tip: You can find Unit Converters for Length, Weight, and more.",
            "Tip: Use the Compound Interest tool to see your savings grow.",
            "Tip: History search helps you find that specific result from last week!"
        )
    }
    
    val randomTip = remember { tips.random() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(AnimationSize)
        )
        Spacer(modifier = Modifier.height(Spacing.l))
        Text(
            text = "Searching...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.s))
        Text(
            text = randomTip,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
