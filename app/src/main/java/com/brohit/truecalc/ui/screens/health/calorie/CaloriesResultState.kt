package com.brohit.truecalc.ui.screens.health.calorie

data class CaloriesResultState(
    /** BMR with two decimals; empty when there is no result. */
    val bmrValue: String = "",
    /** Maintenance calories, rounded; empty when there is no result. */
    val caloriesNeeded: String = "",
    /** Multiplier applied to the BMR for the selected activity level. */
    val activityFactor: Double? = null,
    /** Text for the Share action; null until there is a valid result. */
    val shareText: String? = null,
)
