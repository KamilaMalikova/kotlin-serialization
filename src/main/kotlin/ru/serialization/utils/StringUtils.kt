package ru.serialization.utils

fun String.isAscii(): Boolean =
    none { it.code > 127 }