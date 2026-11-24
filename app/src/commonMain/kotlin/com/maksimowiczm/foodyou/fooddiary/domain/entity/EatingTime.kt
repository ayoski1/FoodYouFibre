package com.maksimowiczm.foodyou.fooddiary.domain.entity

import kotlinx.datetime.LocalTime

/** Local time on the diary date. A second value records an approximate window. */
data class EatingTime(val fromMinute: Int, val toMinute: Int? = null) {
    init {
        require(fromMinute in 0..1439)
        require(toMinute == null || toMinute in 0..1439)
    }

    companion object {
        fun exact(time: LocalTime) = EatingTime(time.hour * 60 + time.minute)

        fun window(from: LocalTime, to: LocalTime) =
            EatingTime(from.hour * 60 + from.minute, to.hour * 60 + to.minute)
    }
}
