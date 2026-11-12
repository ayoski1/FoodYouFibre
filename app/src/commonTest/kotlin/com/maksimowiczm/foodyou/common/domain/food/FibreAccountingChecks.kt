import com.maksimowiczm.foodyou.common.domain.food.NutrientsHelper
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.sum
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue

fun runFibreChecks() {
    val cases = linkedMapOf<String, () -> Unit>(
        "Fibre energy is separate from 20 g available carbohydrate" to {
            check(NutrientsHelper.calculateEnergy(10.0, 20.0, 5.0, 10.0) == 185.0)
        },
        "Float editor calculation includes fibre" to {
            check(NutrientsHelper.calculateEnergy(10f, 20f, 5f, 10f) == 185f)
        },
        "Existing three-macro callers retain their result" to {
            check(NutrientsHelper.calculateEnergy(10.0, 20.0, 5.0) == 165.0)
        },
        "USDA total carbohydrate is normalised once at import" to {
            val available = NutrientsHelper.availableCarbohydrates(30.0, 10.0)
            check(available == 20.0)
            check(NutrientsHelper.calculateEnergy(10.0, available, 5.0, 10.0) == 185.0)
        },
        "Unknown USDA fibre is not assumed zero" to {
            check(NutrientsHelper.availableCarbohydrates(30.0, null) == null)
        },
        "Invalid fibre and carbohydrate pair stays unknown" to {
            check(NutrientsHelper.availableCarbohydrates(5.0, 10.0) == null)
        },
        "Explicit zero fibre is known" to {
            check(NutrientsHelper.availableCarbohydrates(30.0, 0.0) == 30.0)
        },
        "Missing diary fibre preserves the recorded subtotal and incomplete flag" to {
            val sum = NutrientValue.Complete(8.0) + NutrientValue.Incomplete(null)
            check(sum.value == 8.0 && sum.isIncomplete)
        },
        "All unknown fibre remains unknown" to {
            val sum = NutrientValue.Incomplete(null) + NutrientValue.Incomplete(null)
            check(sum.value == null && sum.isIncomplete)
        },
        "Serving scaling preserves missing-data status" to {
            val portion = NutrientValue.Incomplete(4.0) * 3.0
            check(portion.value == 12.0 && portion.isIncomplete)
        },
        "Aggregated recipes retain all-unknown fibre" to {
            val sum = listOf(NutritionFacts(), NutritionFacts()).sum()
            check(sum.dietaryFiber.value == null && sum.dietaryFiber.isIncomplete)
        },
        "Label energy is summed once independently of fibre and its subtypes" to {
            val food = NutritionFacts(energy = NutrientValue.Complete(185.0),
                carbohydrates = NutrientValue.Complete(20.0), dietaryFiber = NutrientValue.Complete(10.0),
                solubleFiber = NutrientValue.Complete(4.0), insolubleFiber = NutrientValue.Complete(6.0))
            val sum = listOf(food, food).sum()
            check(sum.energy.value == 370.0 && sum.carbohydrates.value == 40.0 && sum.dietaryFiber.value == 20.0)
        },
        "Empty diary has known zero totals" to {
            check(emptyList<NutritionFacts>().sum().dietaryFiber == NutrientValue.Complete(0.0))
        },
        "Unknown nutrient list retains null rather than introducing zero" to {
            check(listOf<NutrientValue>(NutrientValue.Incomplete(null)).sum().value == null)
        },
    )
    cases.forEach { (name, run) -> run(); println("PASS: $name") }
    println("${cases.size} checks passed")
}
