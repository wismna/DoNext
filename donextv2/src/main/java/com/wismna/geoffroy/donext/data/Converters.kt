package com.wismna.geoffroy.donext.data

import androidx.room.TypeConverter
import com.wismna.geoffroy.donext.domain.model.Priority
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromPriority(priority: Priority): Int = priority.value

    @TypeConverter
    fun toPriority(value: Int): Priority = Priority.fromValue(value)
}