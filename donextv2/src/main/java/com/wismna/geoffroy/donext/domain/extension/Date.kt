package com.wismna.geoffroy.donext.domain.extension

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
