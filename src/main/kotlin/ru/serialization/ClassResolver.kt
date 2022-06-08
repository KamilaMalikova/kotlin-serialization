package ru.serialization

import ru.serialization.io.Input
import ru.serialization.io.Output
import kotlin.reflect.KClass

interface ClassResolver {
    fun <T : Any> writeClass(output: Output, type: KClass<T>?): Registration?
    fun hasRegistrationWithId(registerId: Int): Boolean
    fun register(registration: Registration): Registration
    fun readClass(input: Input): Registration?
    fun  <T : Any> getRegistration(type: KClass<T>): Registration?
    fun  <T : Any> containsRegistration(type: KClass<T>): Boolean
    fun registerImplicit(type: KClass<*>): Registration
}