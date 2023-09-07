package org.caojun.library.excel

import android.os.Environment
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

        private const val FOLDER_NAME = "XLS"
    }

    fun openFile(fileName: String, folderName: String = FOLDER_NAME): Boolean {
        val file = FileUtils.getSaveFile(Environment.DIRECTORY_DOCUMENTS, folderName, "$fileName.xls")
        val workbook = ReadExcelUtils.getWorkbook(file.absolutePath)
        return workbook != null
    }
}