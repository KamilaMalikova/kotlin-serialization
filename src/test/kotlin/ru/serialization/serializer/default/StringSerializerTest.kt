package ru.serialization.serializer.default

import TestCase
import ru.serialization.serializer.`class`.ClassSerializer
import ru.serialization.serializer.models.SampleClass
import kotlin.reflect.KClass
import kotlin.test.Test

class StringSerializerTest: TestCase() {
    @Test
    fun `test string`() {
        configuration.register(String::class, StringSerializer())
        configuration.register(KClass::class, ClassSerializer())
        println(SampleClass::class::class)
//        roundTrip(6, "meow")
//        roundTrip(70, "abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdef")
//        roundTrip(3, "a")
//        roundTrip(3, "\n")
//        roundTrip(2, "")
//        roundTrip(
//            100,
//            "ABCDEFGHIJKLMNOPQRSTUVWXYZ\rabcdefghijklmnopqrstuvwxyz\n1234567890\t\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*"
//        )
        roundTrip(21, "abcdef\u00E1\u00E9\u00ED\u00F3\u00FA\u7C9F")
    }

    @Test
    fun `test string class`() {
        configuration.register(String::class, StringSerializer())
        roundTripClass(String::class)
    }
}