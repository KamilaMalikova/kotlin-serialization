package ru.serialization.serializer.default

import TestCase
import org.junit.Test

class BooleanSerializerTest: TestCase() {
    @Test
    fun `test boolean`() {
        configuration.register(Boolean::class, BooleanSerializer())
        roundTrip(2, true)
        roundTrip(2, false)
    }
}