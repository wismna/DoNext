package com.wismna.geoffroy.donext.domain.model

enum class Priority(val value: Int, val label: String) {
    LOW(0, "Low"),
    NORMAL(1, "Normal"),
    HIGH(2, "High");

    companion object {
        fun fromValue(value: Int): Priority =
            Priority.entries.firstOrNull { it.value == value } ?: NORMAL
    }
}