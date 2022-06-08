package ru.serialization.serializer.default

import TestCase
import org.junit.Test

class CharSerializerTest: TestCase() {
    @Test
    fun `test char`() {
        configuration.register(Char::class, CharSerializer())
        roundTrip(3, 'a')
        roundTrip(3, 'z')
        roundTrip(3, 'г')
    }
}