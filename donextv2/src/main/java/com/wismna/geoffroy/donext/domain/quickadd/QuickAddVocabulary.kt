package com.wismna.geoffroy.donext.domain.quickadd

import com.wismna.geoffroy.donext.domain.model.Priority
import java.time.DayOfWeek
import java.time.Month

/** English and French words understood by quick add, in normalized form (lowercase, no accents). */
internal object QuickAddVocabulary {
    val TODAY = setOf("today", "aujourd'hui", "aujourdhui")
    val TOMORROW = setOf("tomorrow", "demain")
    val AFTER_TOMORROW_SINGLE = setOf("apres-demain")
    val AFTER_TOMORROW_PHRASES = listOf(
        listOf("day", "after", "tomorrow"),
        listOf("apres", "demain")
    )
    val NEXT_WEEK_PHRASES = listOf(
        listOf("next", "week"),
        listOf("semaine", "prochaine")
    )
    val IN = setOf("in", "dans")
    val DAY_UNITS = setOf("day", "days", "jour", "jours")
    val WEEK_UNITS = setOf("week", "weeks", "semaine", "semaines")
    const val NEXT_EN = "next"
    val NEXT_FR = setOf("prochain", "prochaine")
    val AT = setOf("at", "a")
    val DATE_PREPOSITIONS = setOf("on", "le")

    val WEEKDAYS: Map<String, DayOfWeek> = mapOf(
        "monday" to DayOfWeek.MONDAY, "lundi" to DayOfWeek.MONDAY,
        "tuesday" to DayOfWeek.TUESDAY, "mardi" to DayOfWeek.TUESDAY,
        "wednesday" to DayOfWeek.WEDNESDAY, "mercredi" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY, "jeudi" to DayOfWeek.THURSDAY,
        "friday" to DayOfWeek.FRIDAY, "vendredi" to DayOfWeek.FRIDAY,
        "saturday" to DayOfWeek.SATURDAY, "samedi" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY, "dimanche" to DayOfWeek.SUNDAY
    )

    val MONTHS: Map<String, Month> = mapOf(
        "january" to Month.JANUARY, "jan" to Month.JANUARY, "janvier" to Month.JANUARY, "janv" to Month.JANUARY,
        "february" to Month.FEBRUARY, "feb" to Month.FEBRUARY, "fevrier" to Month.FEBRUARY, "fevr" to Month.FEBRUARY,
        "march" to Month.MARCH, "mar" to Month.MARCH, "mars" to Month.MARCH,
        "april" to Month.APRIL, "apr" to Month.APRIL, "avril" to Month.APRIL, "avr" to Month.APRIL,
        "may" to Month.MAY, "mai" to Month.MAY,
        "june" to Month.JUNE, "jun" to Month.JUNE, "juin" to Month.JUNE,
        "july" to Month.JULY, "jul" to Month.JULY, "juillet" to Month.JULY, "juil" to Month.JULY,
        "august" to Month.AUGUST, "aug" to Month.AUGUST, "aout" to Month.AUGUST,
        "september" to Month.SEPTEMBER, "sep" to Month.SEPTEMBER, "sept" to Month.SEPTEMBER,
        "septembre" to Month.SEPTEMBER,
        "october" to Month.OCTOBER, "oct" to Month.OCTOBER, "octobre" to Month.OCTOBER,
        "november" to Month.NOVEMBER, "nov" to Month.NOVEMBER, "novembre" to Month.NOVEMBER,
        "december" to Month.DECEMBER, "dec" to Month.DECEMBER, "decembre" to Month.DECEMBER
    )

    val PRIORITIES: Map<String, Priority> = mapOf(
        "!high" to Priority.HIGH, "!haute" to Priority.HIGH, "!!" to Priority.HIGH,
        "!normal" to Priority.NORMAL, "!normale" to Priority.NORMAL,
        "!low" to Priority.LOW, "!basse" to Priority.LOW
    )
}
