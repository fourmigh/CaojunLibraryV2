package org.caojun.library.excel

import android.content.Context
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.caojun.library.excel.enums.Order
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

    fun openFile(context: Context, fileName: String, folderName: String = FOLDER_NAME, extensionName: String = EXTENSION_NAME): XSSFWorkbook? {
        val file = FileUtils.getSaveFile(context, folderName, "$fileName.$extensionName", false)
        return ReadExcelUtils.getWorkbook(file.absolutePath)
    }

    private fun getSheet(workbook: XSSFWorkbook, order: Order, maxRowsPerSheet: Int): XSSFSheet? {
        val numberOfSheets = ReadExcelUtils.getNumberOfSheets(workbook)
        val currentSheetIndex = numberOfSheets - 1
        if (currentSheetIndex < 0) {
            return null
        }
        var sheet = workbook.getSheetAt(currentSheetIndex)
        if (ReadExcelUtils.getNumberOfRows(sheet) >= maxRowsPerSheet) {
            sheet = WriteExcelUtils.createSheet(workbook)
        } else if (order == Order.DESC) {
            WriteExcelUtils.shiftRows(sheet)
        }
        return sheet
    }

    private fun writeRow(sheet: XSSFSheet, cells: List<String>, rowNumber: Int): Boolean {
        val row = sheet.createRow(rowNumber)
        return WriteExcelUtils.writeRow(row, cells)
    }

    fun insert(workbook: XSSFWorkbook, cells: List<String>, maxRowsPerSheet: Int): Boolean {
        val sheet = getSheet(workbook, Order.DESC, maxRowsPerSheet) ?: return false
        return writeRow(sheet, cells, 0)
    }

    fun add(workbook: XSSFWorkbook, cells: List<String>, maxRowsPerSheet: Int): Boolean {
        val sheet = getSheet(workbook, Order.ASC, maxRowsPerSheet) ?: return false
        return writeRow(sheet, cells, ReadExcelUtils.getNumberOfRows(sheet))
    }

    fun saveFile() {
        val workbooks = ReadExcelUtils.getWorkbooks()
        for (workbook in workbooks) {
            WriteExcelUtils.save(workbook)
        }
    }
}