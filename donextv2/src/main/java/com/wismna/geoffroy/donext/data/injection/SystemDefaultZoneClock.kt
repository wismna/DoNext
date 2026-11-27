package com.wismna.geoffroy.donext.data.injection

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Unlike Clock.systemDefaultZone(), which captures the zone once, this resolves the system zone on every
 * call so a long-lived process follows time zone changes.
 */
class SystemDefaultZoneClock : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): Clock = system(zone)

    override fun instant(): Instant = Instant.now()
}
