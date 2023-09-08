package org.caojun.library.excel.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.caojun.library.room.RoomUtils

@Database(entities = [ExcelLog::class], version = 1, exportSchema = false)
abstract class ExcelLogDatabase : RoomDatabase() {

    abstract fun getDao(): ExcelLogDao

    companion object {
        private var INSTANCE: ExcelLogDatabase? = null

        @JvmStatic fun getDatabase(context: Context): ExcelLogDatabase {
            if (INSTANCE == null) {
                val file = RoomUtils.getDatabaseFile(context, "excellog_database.db")
                INSTANCE = Room.databaseBuilder(context.applicationContext, ExcelLogDatabase::class.java, file.absolutePath)
                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE!!
        }

        @JvmStatic fun destroyInstance() {
            INSTANCE = null
        }
    }
}