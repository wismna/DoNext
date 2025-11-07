package com.wismna.geoffroy.donext.domain.extension

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Long.toLocalDate(clock: Clock = Clock.systemDefaultZone()): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(clock.zone)
        .toLocalDate()
