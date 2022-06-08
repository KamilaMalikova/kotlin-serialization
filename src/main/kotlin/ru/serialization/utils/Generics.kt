package ru.serialization.utils

import kotlin.reflect.KClass

interface Generics {
    fun nextGenericClass(): KClass<*>?
}