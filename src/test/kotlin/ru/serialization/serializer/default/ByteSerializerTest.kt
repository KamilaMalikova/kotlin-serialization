package ru.serialization.serializer.default

import TestCase
import org.junit.Test

class ByteSerializerTest: TestCase() {
    @Test
    fun `test byte`() {
        configuration.register(Byte::class, ByteSerializer())
        roundTrip(2, 1.toByte())
        roundTrip(2, 125.toByte())
        roundTrip(2, (-125).toByte())
    }
}