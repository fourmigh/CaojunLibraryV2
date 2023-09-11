package org.caojun.library.excel

import android.content.Context
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.caojun.library.file.FileUtils
import org.caojun.library.klog.KLog

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

    fun openFile(context: Context, fileName: String, folderName: String = FOLDER_NAME, extensionName: String = EXTENSION_NAME): XSSFWorkbook? {
        val file = FileUtils.getSaveFile(context, folderName, "$fileName.$extensionName", false)
        return ReadExcelUtils.getWorkbook(file.absolutePath)
    }

    fun insert(workbook: XSSFWorkbook, cells: List<String>) {
        val sheet = workbook.getSheetAt(0)
        WriteExcelUtils.shiftRows(sheet)
        val row = sheet.createRow(0)
        WriteExcelUtils.writeRow(row, cells)
    }

    fun saveFile() {
        val workbooks = ReadExcelUtils.getWorkbooks()
        for (workbook in workbooks) {
            WriteExcelUtils.save(workbook)
        }
    }
}