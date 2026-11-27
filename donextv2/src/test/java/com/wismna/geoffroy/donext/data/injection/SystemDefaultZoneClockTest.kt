package com.wismna.geoffroy.donext.data.injection

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import java.time.ZoneId
import java.util.TimeZone

class SystemDefaultZoneClockTest {

    private val originalTimeZone: TimeZone = TimeZone.getDefault()

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `follows a system time zone change after creation`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
        val clock = SystemDefaultZoneClock()

        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))

        assertThat(clock.zone).isEqualTo(ZoneId.of("America/New_York"))
    }
}
