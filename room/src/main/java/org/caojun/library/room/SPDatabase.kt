package org.caojun.library.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.caojun.library.room.sp.*

@Database(entities = [
    BooleanData::class,
    BooleanArrayData::class,
    FloatData::class,
    FloatArrayData::class,
    IntData::class,
    IntArrayData::class,
    LongData::class,
    LongArrayData::class,
    StringData::class,
    StringArrayData::class,
    ByteData::class,
    ByteArrayData::class], version = 1, exportSchema = false)
abstract class SPDatabase : RoomDatabase() {
    abstract fun booleanDao(): BooleanDao
    abstract fun booleanArrayDao(): BooleanArrayDao
    abstract fun floatDao(): FloatDao
    abstract fun floatArrayDao(): FloatArrayDao
    abstract fun intDao(): IntDao
    abstract fun intArrayDao(): IntArrayDao
    abstract fun longDao(): LongDao
    abstract fun longArrayDao(): LongArrayDao
    abstract fun stringDao(): StringDao
    abstract fun stringArrayDao(): StringArrayDao
    abstract fun byteDao(): ByteDao
    abstract fun byteArrayDao(): ByteArrayDao

    companion object {
        private var INSTANCE: SPDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): SPDatabase {
            if (INSTANCE == null) {
                val file = RoomUtils.getDatabaseFile(context, "SharedPreferences.db")
                INSTANCE = Room.databaseBuilder(context.applicationContext, SPDatabase::class.java, file.absolutePath)
                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE!!
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}