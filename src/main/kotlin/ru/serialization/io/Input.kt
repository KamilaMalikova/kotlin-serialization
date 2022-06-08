package ru.serialization.io

abstract class Input {
    abstract fun readByte(): Byte
    abstract fun readBoolean(): Boolean
    abstract fun readChar(): Char
    abstract fun readShort(): Short
    abstract fun readInt(optimizePositive: Boolean = true): Int
    abstract fun readLong(): Long
    abstract fun readString(): String?
    abstract fun readVarInt(optimizePositive: Boolean = true): Int
    abstract fun readVarLong(optimizePositive: Boolean = true): Long
    abstract fun readFloat(): Float
    abstract fun readDouble(): Double
    abstract fun readBytes(length: Int): ByteArray
}