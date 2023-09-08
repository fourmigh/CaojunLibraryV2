package org.caojun.library.excel

import android.content.Context
import android.os.Environment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.caojun.library.file.FileUtils

class ExcelManager {

    companion object {
        private var excelManager: ExcelManager? = null

        @JvmStatic
        fun getInstance(): ExcelManager {
            if (excelManager == null) {
                synchronized(ExcelManager::class.java) {
                    if (excelManager == null) {
                        excelManager = ExcelManager()
                    }
                }
            }
            return excelManager!!
        }

        private const val FOLDER_NAME = "XLSX"
        private const val EXTENSION_NAME = "xlsx"
    }

    fun openFile(context: Context, fileName: String, folderName: String = FOLDER_NAME): XSSFWorkbook? {
        val file = FileUtils.getSaveFile(context, folderName, "$fileName.$EXTENSION_NAME", false)
        return ReadExcelUtils.getWorkbook(file.absolutePath)
    }
}