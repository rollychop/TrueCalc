package com.brohit.truecalc.ui.screens.home

import androidx.compose.ui.graphics.vector.ImageVector
import com.brohit.truecalc.ui.navigation.Route

data class Calculator(
    val name: String,
    val description: String,
    val route: Route,
    val icon: ImageVector
)