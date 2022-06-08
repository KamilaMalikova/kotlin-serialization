package ru.serialization.exception

class BufferOverflowException(
    override val message: String = ""
): SerializationException() {
}