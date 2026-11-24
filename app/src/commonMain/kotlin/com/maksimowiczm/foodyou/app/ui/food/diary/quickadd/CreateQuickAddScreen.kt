package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.diary.component.EatingTimeDialog
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreateQuickAddScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    mealId: Long,
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val viewModel: CreateQuickAddViewModel = koinViewModel { parametersOf(date, mealId) }
    val energyFormatter = LocalEnergyFormatter.current

    val latestOnSave by rememberUpdatedState(onSave)
    LaunchedCollectWithLifecycle(viewModel.uiEvents) {
        when (it) {
            QuickAddUiEvent.Saved -> latestOnSave()
        }
    }

    val formState = rememberQuickAddFormState()

    val mealRepository: MealRepository = koinInject()
    val meal by remember(mealRepository, mealId) {
        mealRepository.observeMeal(mealId)
    }.collectAsStateWithLifecycle(initialValue = null)
    var showTime by rememberSaveable { mutableStateOf(false) }

    val selectedMeal = meal
    if (showTime && selectedMeal != null) {
        EatingTimeDialog(date, selectedMeal, onDismiss = { showTime = false }) { time ->
            showTime = false
            val name = formState.name.value
            val energy = formState.energy.value?.let(energyFormatter::toKcal) ?: 0.0
            val proteins = formState.proteins.value ?: 0.0
            val carbohydrates = formState.carbohydrates.value ?: 0.0
            val fats = formState.fats.value ?: 0.0

            viewModel.addEntry(
                name = name,
                energy = energy,
                proteins = proteins,
                carbohydrates = carbohydrates,
                fats = fats,
                eatingTime = time,
            )
        }
    }

    QuickAddScreen(
        onBack = onBack,
        onSave = { if (selectedMeal != null) showTime = true },
        modifier = modifier,
        state = formState,
    )
}
