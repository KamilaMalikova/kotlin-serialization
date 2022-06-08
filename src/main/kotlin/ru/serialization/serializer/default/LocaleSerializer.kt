package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.util.*
import kotlin.reflect.KClass

class LocaleSerializer: Serializer<Locale>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: Locale) {
        TODO("Not yet implemented")
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): Locale? {
        TODO("Not yet implemented")
    }
}