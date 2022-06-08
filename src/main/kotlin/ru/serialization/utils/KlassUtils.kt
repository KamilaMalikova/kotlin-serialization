package ru.serialization.utils

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

val<T: Any> T.kClass: KClass<T>
    get() = javaClass.kotlin

fun KClass<*>.isEnum() = isSubclassOf(Enum::class)

fun KClass<*>.isAssignableToEnumSet() = isSubclassOf(EnumSet::class)

fun KClass<*>.isAssignableFrom(type: KClass<*>) = isSubclassOf(type)

fun KClass<*>.isPrimitive() = primitives.contains(this)

private val primitives: Set<KClass<out Any>> = setOf(
    Boolean::class,
    Char::class,
    Byte::class,
    Short::class,
    Int::class,
    Long::class,
    Float::class,
    Double::class,
    String::class,
    Unit::class
)