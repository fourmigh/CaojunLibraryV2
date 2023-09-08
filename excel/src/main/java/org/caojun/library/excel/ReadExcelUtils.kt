package org.caojun.library.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.caojun.library.file.FileUtils

object ReadExcelUtils {

    private val hmWorkbook = HashMap<String, XSSFWorkbook>()

    init {
        init()
    }

    private fun init() {
        hmWorkbook.clear()
    }

    fun refresh(workbook: XSSFWorkbook) {
        val key = getFilePath(workbook) ?: return
        hmWorkbook.remove(key)
    }

    /**
     * 第一层：Workbook
     */
    fun getWorkbook(filePath: String): XSSFWorkbook? {
        val key: String = filePath
        if (hmWorkbook.containsKey(key)) {
            return hmWorkbook[key]
        }
        val inputStream = FileUtils.getInputStream(filePath) ?: return null
        val workbook = XSSFWorkbook(inputStream)
        hmWorkbook[key] = workbook
        return workbook
    }

    fun getFilePath(workbook: XSSFWorkbook): String? {
        for (filePath in hmWorkbook.keys) {
            if (hmWorkbook[filePath] == workbook) {
                return filePath
            }
        }
        return null
    }

    fun getNumberOfSheets(filePath: String): Int {
        val workbook = getWorkbook(filePath) ?: return 0
        return workbook.numberOfSheets
    }

    /**
     * 第二层：Sheet
     */
    fun getSheet(workbook: XSSFWorkbook, indexSheet: Int): XSSFSheet? {
        if (indexSheet < 0) {
            return null
        }
        if (indexSheet >= workbook.numberOfSheets) {
            return null
        }
        return workbook.getSheetAt(indexSheet)
    }

    fun getNumberOfRows(sheet: XSSFSheet?): Int {
        return sheet?.physicalNumberOfRows ?: 0
    }

    /**
     * 第三层：Row
     */
    fun getRow(sheet: XSSFSheet, indexRow: Int): Row? {
        if (indexRow < 0) {
            return null
        }
        if (indexRow >= sheet.physicalNumberOfRows) {
            return null
        }
        return sheet.getRow(indexRow)
    }

    fun getNumberOfCells(row: Row?): Int {
        return row?.physicalNumberOfCells ?: 0
    }

    /**
     * 第四层：Cell
     */
    fun getCell(row: Row, indexCell: Int): Cell? {
        if (indexCell < 0) {
            return null
        }
        return row.getCell(indexCell)
    }
}