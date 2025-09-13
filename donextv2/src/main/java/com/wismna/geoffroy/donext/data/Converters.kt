package com.wismna.geoffroy.donext.data

import androidx.room.TypeConverter
import com.wismna.geoffroy.donext.domain.model.Priority

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): Int = priority.value

    @TypeConverter
    fun toPriority(value: Int): Priority = Priority.fromValue(value)
}