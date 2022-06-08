package ru.serialization.exception

import java.io.IOException

class DefaultException(
    override val message: String? = null,
    override val cause: Throwable? = null
): SerializationException() {
}