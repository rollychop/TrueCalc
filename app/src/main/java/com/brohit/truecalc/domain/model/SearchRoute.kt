package com.brohit.truecalc.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.brohit.truecalc.ui.navigation.Route

data class SearchRoute(
    val route: Route,
    val category: String,
    val label: String,
    val description: String,
    val icon: ImageVector
)