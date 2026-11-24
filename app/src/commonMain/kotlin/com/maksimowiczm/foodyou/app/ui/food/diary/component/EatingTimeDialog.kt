package com.maksimowiczm.foodyou.app.ui.food.diary.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.fooddiary.domain.entity.EatingTime
import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
internal fun EatingTimeDialog(
    date: LocalDate,
    meal: Meal,
    onDismiss: () -> Unit,
    onSave: (EatingTime) -> Unit,
) {
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    val isToday = date == LocalDate.now()

    if (pickingTime) {
        val initial = if (isToday) LocalTime.now() else meal.from
        val time = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
        TimePickerDialog(
            title = { Text("Time eaten") },
            onDismissRequest = { pickingTime = false },
            confirmButton = {
                TextButton(onClick = { onSave(EatingTime.exact(LocalTime(time.hour, time.minute))) }) {
                    Text("Save entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingTime = false }) { Text("Back") }
            },
        ) {
            TimePicker(time)
        }
    } else {
        val window = EatingTime.window(meal.from, meal.to)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("When did you eat this?") },
            text = {
                Column {
                    if (isToday) {
                        TextButton(onClick = { onSave(EatingTime.exact(LocalTime.now())) }) {
                            Text("Ate it now")
                        }
                    }
                    TextButton(onClick = { pickingTime = true }) { Text("Pick a time") }
                    TextButton(onClick = { onSave(window) }) {
                        val label = if (meal.from == meal.to) {
                            "Use the whole day"
                        } else {
                            "Use meal window (${meal.from.toString().take(5)}–${meal.to.toString().take(5)})"
                        }
                        Text(label)
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        )
    }
}
