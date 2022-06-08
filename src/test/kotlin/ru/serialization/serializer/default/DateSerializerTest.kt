package ru.serialization.serializer.default

import TestCase
import org.junit.Test
import java.sql.Time
import java.util.*


class DateSerializerTest: TestCase() {
    @Test
    fun `test date`() {
        configuration.register(Date::class, DateSerializer())
        roundTrip(10, Date(-1234567))
        roundTrip(2, Date(0))
        roundTrip(4, Date(1234567))
        roundTrip(10, Date(-1234567))

        roundTrip(10, java.sql.Date(Long.MIN_VALUE))
        roundTrip(2, java.sql.Date(0))
        roundTrip(4, java.sql.Date(1234567))
        roundTrip(10, java.sql.Date(Long.MAX_VALUE))
        roundTrip(10, java.sql.Date(-1234567))

        roundTrip(10, Time(Long.MIN_VALUE))
        roundTrip(2, Time(0))
        roundTrip(4, Time(1234567))
        roundTrip(10, Time(Long.MAX_VALUE))
        roundTrip(10, Time(-1234567))
    }
}