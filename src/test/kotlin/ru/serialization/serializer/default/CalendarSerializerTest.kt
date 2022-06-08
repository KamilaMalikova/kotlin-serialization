package ru.serialization.serializer.default

import TestCase
import org.junit.Test
import java.util.*

class CalendarSerializerTest: TestCase() {
    @Test
    fun `test calendar`() {
        configuration.register(Calendar::class, CalendarSerializer())
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
        calendar[1980, 7, 26, 12, 22] = 46
        roundTrip(64, calendar)
    }
}