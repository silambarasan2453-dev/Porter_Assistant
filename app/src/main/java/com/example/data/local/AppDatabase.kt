package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.model.RangeClassification

class Converters {
    @TypeConverter
    fun fromClassification(classification: RangeClassification?): String? {
        return classification?.name
    }

    @TypeConverter
    fun toClassification(name: String?): RangeClassification {
        return try {
            if (name != null) RangeClassification.valueOf(name) else RangeClassification.UNKNOWN
        } catch (e: Exception) {
            RangeClassification.UNKNOWN
        }
    }
}

@Database(entities = [DetectionRecord::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun detectionDao(): DetectionDao
}
