package ru.serialization.io

abstract class Output {
    abstract fun writeNull()
    abstract fun writeByte(serializableValue: Byte)
    abstract fun writeBoolean(serializableValue: Boolean)
    abstract fun writeChar(serializableValue: Char)
    abstract fun writeShort(serializableValue: Short)
    abstract fun writeInt(serializableValue: Int, optimizePositive: Boolean = true): Int
    abstract fun writeLong(serializableValue: Long)
    abstract fun writeString(serializableValue: String?)
    abstract fun writeVarInt(value: Int, optimizePositive: Boolean = true): Int
    abstract fun writeVarIntFlag(flag: Boolean, value: Int, optimizePositive: Boolean): Int
    abstract fun writeVarLong(serializableValue: Long, optimizePositive: Boolean = true): Int
    abstract fun writeFloat(serializableValue: Float)
    abstract fun writeDouble(serializableValue: Double)
    abstract fun writeBytes(bytes: ByteArray)
    abstract fun flush()
}