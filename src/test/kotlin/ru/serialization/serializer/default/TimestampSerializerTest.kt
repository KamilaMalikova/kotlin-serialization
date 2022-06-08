package ru.serialization.serializer.default

import TestCase
import org.junit.Test
import java.sql.Timestamp
import java.util.*

class TimestampSerializerTest: TestCase() {
    private fun newTimestamp(time: Long, nanos: Int): Timestamp =
        Timestamp(time).also { it.nanos = nanos }

    @Test
    fun `test timestamp`() {
        configuration.register(Date::class, DateSerializer())
        configuration.register(Timestamp::class, TimestampSerializer())
        roundTrip(11, newTimestamp(Long.MIN_VALUE + 808, 0)) // Smallest valid size

        roundTrip(15, newTimestamp(Long.MIN_VALUE + 808, 999999999))
        roundTrip(11, newTimestamp(Long.MAX_VALUE, 0))
        roundTrip(14, newTimestamp(Long.MAX_VALUE, 268435455)) // Largest valid size

        roundTrip(3, newTimestamp(0, 0))
        roundTrip(7, newTimestamp(0, 999999999))
        roundTrip(8, newTimestamp(1234567, 123456789))
        roundTrip(11, newTimestamp(-1234567, 0))
        roundTrip(11, newTimestamp(-1234567, 1))
        roundTrip(14, newTimestamp(-1234567, 123456789))
    }
}