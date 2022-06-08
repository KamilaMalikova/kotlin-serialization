package ru.serialization.serializer.default

import TestCase
import org.junit.Test

class LongSerializerTest: TestCase() {
    @Test
    fun `test long`() {
        configuration.register(Long::class, LongSerializer())
        roundTrip(2, 0L)
        roundTrip(2, 63L)
        roundTrip(3, 64L)
        roundTrip(3, 127L)
        roundTrip(3, 128L)
        roundTrip(3, 8191L)
        roundTrip(4, 8192L)
        roundTrip(4, 16383L)
        roundTrip(4, 16384L)
        roundTrip(5, 2097151L)
        roundTrip(4, 1048575L)
        roundTrip(5, 134217727L)
        roundTrip(6, 268435455L)
        roundTrip(6, 134217728L)
        roundTrip(6, 268435456L)
        roundTrip(2, -64L)
        roundTrip(3, -65L)
        roundTrip(3, -8192L)
        roundTrip(4, -1048576L)
        roundTrip(5, -134217728L)
        roundTrip(6, -134217729L)
        roundTrip(10, 2368365495612416452L)
        roundTrip(10, -2368365495612416452L)
    }
}