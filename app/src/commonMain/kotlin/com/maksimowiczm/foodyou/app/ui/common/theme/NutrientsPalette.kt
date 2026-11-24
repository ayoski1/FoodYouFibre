package com.maksimowiczm.foodyou.app.ui.common.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Immutable
data class NutrientsPalette(
    val proteinsOnSurfaceContainer: Color = Color.Unspecified,
    val carbohydratesOnSurfaceContainer: Color = Color.Unspecified,
    val fatsOnSurfaceContainer: Color = Color.Unspecified,
    val fibreOnSurfaceContainer: Color = Color.Unspecified,
)

val DarkNutrientsPalette =
    NutrientsPalette(
        proteinsOnSurfaceContainer = Color(0xFFFF9090),
        carbohydratesOnSurfaceContainer = Color(0xFF83D883),
        fatsOnSurfaceContainer = Color(0xFF83B4FF),
        fibreOnSurfaceContainer = Color(0xFFBAC0C7),
    )

val LightNutrientsPalette =
    NutrientsPalette(
        proteinsOnSurfaceContainer = Color(0xFFFF0000),
        carbohydratesOnSurfaceContainer = Color(0xFF006400),
        fatsOnSurfaceContainer = Color(0xFF0066FF),
        fibreOnSurfaceContainer = Color(0xFF5E6268),
    )

val LocalNutrientsPalette = staticCompositionLocalOf { NutrientsPalette() }
