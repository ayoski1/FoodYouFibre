package com.maksimowiczm.foodyou.common.domain.food

object NutrientsHelper {
    const val PROTEINS = 4
    const val CARBOHYDRATES = 4
    const val FATS = 9
    const val DIETARY_FIBER = 2

    fun calculateEnergy(proteins: Float, carbohydrates: Float, fats: Float, dietaryFiber: Float = 0f): Float =
        proteins * PROTEINS + carbohydrates * CARBOHYDRATES + fats * FATS + dietaryFiber * DIETARY_FIBER

    fun calculateEnergy(proteins: Double, carbohydrates: Double, fats: Double, dietaryFiber: Double = 0.0): Double =
        proteins * PROTEINS + carbohydrates * CARBOHYDRATES + fats * FATS + dietaryFiber * DIETARY_FIBER

    /** USDA total carbohydrate includes fibre. Unknown fibre stays unknown, never zero. */
    fun availableCarbohydrates(total: Double?, dietaryFiber: Double?): Double? =
        if (total == null || dietaryFiber == null || dietaryFiber > total || total < 0 || dietaryFiber < 0) null
        else (total - dietaryFiber).coerceAtLeast(0.0)

    fun proteinsPercentage(energy: Int, proteins: Number): Float =
        proteins.toFloat() * PROTEINS / energy

    fun carbohydratesPercentage(energy: Int, carbohydrates: Number): Float =
        carbohydrates.toFloat() * CARBOHYDRATES / energy

    fun fatsPercentage(energy: Int, fats: Number) = fats.toFloat() * FATS / energy

    fun proteinsPercentageToGrams(energy: Int, proteinsPercentage: Double): Double =
        (proteinsPercentage * energy) / PROTEINS

    fun carbohydratesPercentageToGrams(energy: Int, carbohydratesPercentage: Double): Double =
        (carbohydratesPercentage * energy) / CARBOHYDRATES

    fun fatsPercentageToGrams(energy: Int, fatsPercentage: Double): Double =
        (fatsPercentage * energy) / FATS

    fun caloriesToKilojoules(calories: Number): Double = calories.toDouble() * 4.184
}
