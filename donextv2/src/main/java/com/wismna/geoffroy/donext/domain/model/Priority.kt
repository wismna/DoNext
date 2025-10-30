package com.wismna.geoffroy.donext.domain.model

import com.wismna.geoffroy.donext.R

enum class Priority(val value: Int, val label: Int) {
    LOW(0, R.string.task_priority_low),
    NORMAL(1, R.string.task_priority_normal),
    HIGH(2, R.string.task_priority_high);

    companion object {
        fun fromValue(value: Int): Priority =
            Priority.entries.firstOrNull { it.value == value } ?: NORMAL
    }
}