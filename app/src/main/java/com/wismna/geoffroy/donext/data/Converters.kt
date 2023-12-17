package com.wismna.geoffroy.donext.data;

import org.joda.time.LocalDate;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static LocalDate fromDateString(String value) {
        return LocalDate.parse(value);
    }

    @TypeConverter
    public static String toLocalDate(LocalDate value) {
        return value.toString();
    }
}
