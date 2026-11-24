package com.maksimowiczm.foodyou.app.ui.common.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Immutable
data class NutrientsPalette(
    val proteinsOnSurfaceContainer: Color = Color.Unspecified,
    val carbohydratesOnSurfaceContainer: Color = Color.Unspecified,
    val fatsOnSurfaceContainer: Color = Color.Unspecified,
)

val DarkNutrientsPalette =
    NutrientsPalette(
        proteinsOnSurfaceContainer = Color(0xFFFF9090),
        carbohydratesOnSurfaceContainer = Color(0xFF83D883),
        fatsOnSurfaceContainer = Color(0xFF83B4FF),
    )

val LightNutrientsPalette =
    NutrientsPalette(
        proteinsOnSurfaceContainer = Color(0xFFFF0000),
        carbohydratesOnSurfaceContainer = Color(0xFF006400),
        fatsOnSurfaceContainer = Color(0xFF0066FF),
    )

val LocalNutrientsPalette = staticCompositionLocalOf { NutrientsPalette() }
