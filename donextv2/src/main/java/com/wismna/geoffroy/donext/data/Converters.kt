package com.wismna.geoffroy.donext.data

import androidx.room.TypeConverter
import com.wismna.geoffroy.donext.domain.model.Priority
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): Int = priority.value

    @TypeConverter
    fun toPriority(value: Int): Priority = Priority.fromValue(value)

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }
}