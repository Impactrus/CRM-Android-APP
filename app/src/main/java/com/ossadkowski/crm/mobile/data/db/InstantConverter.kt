package com.ossadkowski.crm.mobile.data.db

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room TypeConverter for [java.time.Instant] ↔ epoch-millis [Long]. Null-safe.
 */
class InstantConverter {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}
