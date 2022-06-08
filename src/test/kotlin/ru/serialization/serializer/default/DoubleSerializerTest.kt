package ru.serialization.serializer.default

import TestCase
import org.junit.Test

class DoubleSerializerTest: TestCase() {
    @Test
    fun `test double`() {
        configuration.register(Double::class, DoubleSerializer())
        roundTrip(9, 0.0)
        roundTrip(9, 1234.0)
        roundTrip(9, 1234.5678)
    }
}