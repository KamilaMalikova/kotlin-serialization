package ru.serialization.serializer.default

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.Output
import ru.serialization.serializer.Serializer
import java.util.*
import kotlin.reflect.KClass

class TreeMapSerializer: Serializer<TreeMap<out Any, out  Any>>() {
    override fun write(configuration: Configuration, output: Output, serializableValue: TreeMap<out Any, out Any>) {
        TODO("Not yet implemented")
    }

    override fun read(configuration: Configuration, input: Input, type: KClass<out Any>): TreeMap<Any, Any>? {
        TODO("Not yet implemented")
    }
}