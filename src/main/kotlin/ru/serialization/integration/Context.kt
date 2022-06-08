package ru.serialization.integration

import ru.serialization.Configuration
import ru.serialization.io.Input
import ru.serialization.io.InputChunked
import ru.serialization.io.Output
import ru.serialization.io.OutputChunked

class Context(
    val buffer: Int,
    val configuration: Configuration,
    val inputChunked: InputChunked,
    val outputChunked: OutputChunked
)