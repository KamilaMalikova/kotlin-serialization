package ru.serialization.serializer.default

import TestCase
import org.junit.Test

class FloatSerializerTest: TestCase() {
    @Test
    fun `test float`() {
        configuration.register(Float::class, FloatSerializer())
        roundTrip(5, 0f)
        roundTrip(5, 123f)
        roundTrip(5, 123.456f)
    }
}