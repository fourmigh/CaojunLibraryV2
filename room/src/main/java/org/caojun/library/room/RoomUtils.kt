package org.caojun.library.room

import android.content.Context
import org.caojun.library.file.FileUtils
import java.io.File

object RoomUtils {

    fun getDatabaseFile(context: Context, fileName: String = "database.db", folderName: String = "Database"): File {

        return FileUtils.getSaveFile(context, folderName, fileName)
    }
}