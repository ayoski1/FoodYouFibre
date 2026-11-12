package com.maksimowiczm.foodyou.app.ui.home.goals

import androidx.compose.runtime.*

@Immutable
internal data class DaySummaryModel(
    val energy: Int,
    val energyGoal: Int,
    val proteins: Int,
    val proteinsGoal: Int,
    val carbohydrates: Int,
    val carbohydratesGoal: Int,
    val fats: Int,
    val fatsGoal: Int,
    val dietaryFiber: Double? = null,
    val dietaryFiberComplete: Boolean = false,
    val dietaryFiberGoal: Double = 30.0,
    val solubleFiber: Double? = null,
    val insolubleFiber: Double? = null,
    val solubleFiberComplete: Boolean = false,
    val insolubleFiberComplete: Boolean = false,
)
