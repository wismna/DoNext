package com.wismna.geoffroy.donext.data

import androidx.room.TypeConverter
import org.joda.time.LocalDate

object Converters {
    @JvmStatic
    @TypeConverter
    fun fromDateString(value: String?): LocalDate {
        return LocalDate.parse(value)
    }

    @JvmStatic
    @TypeConverter
    fun toLocalDate(value: LocalDate): String {
        return value.toString()
    }
}
