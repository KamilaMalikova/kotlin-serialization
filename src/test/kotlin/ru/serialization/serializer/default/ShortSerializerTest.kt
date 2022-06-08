package ru.serialization.serializer.default

import TestCase
import org.junit.Test

class ShortSerializerTest: TestCase() {
    @Test
    fun `test short`() {
        configuration.register(Short::class, ShortSerializer())
        roundTrip(3, 0.toShort())
        roundTrip(3, 123.toShort())
        roundTrip(3, 123.toShort())
        roundTrip(3, (-123).toShort())
        roundTrip(3, 250.toShort())
        roundTrip(3, 123.toShort())
        roundTrip(3, 400.toShort())
    }
}