package ru.serialization.integration

import ru.serialization.Configuration
import kotlin.reflect.KClass

interface UserSerializer {
    fun registerSingleSerializer(configuration: Configuration, type: KClass<out Any>)

    fun registerAllSerializers(configuration: Configuration)
}