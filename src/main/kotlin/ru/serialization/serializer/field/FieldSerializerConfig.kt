package ru.serialization.serializer.field

class FieldSerializerConfig(
    val fieldsCanBeNull: Boolean = true,
    val setFieldsAsAccessible: Boolean = true,
    val ignoreSyntheticFields: Boolean = true,
    val fixedFieldTypes: Boolean = false,
    val copyTransient: Boolean = true,
    val serializeTransient: Boolean = false,
    val varEncoding: Boolean = true,
    val extendedFieldNames: Boolean = false
)