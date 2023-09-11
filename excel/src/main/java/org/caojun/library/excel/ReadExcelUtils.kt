package org.caojun.library.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.caojun.library.file.FileUtils
import java.io.File
import java.io.FileOutputStream

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

    private fun readWorkbook(filePath: String): XSSFWorkbook? {
        return try {
            val inputStream = FileUtils.getInputStream(filePath)
            XSSFWorkbook(inputStream)
        } catch (e: Exception) {
            return try {
                val file = File(filePath)
                val workbook = XSSFWorkbook()
                workbook.createSheet()
//                val sheet = workbook.createSheet()
//                val row = sheet.createRow(0)
//                val cell = row.createCell(0)
//                cell.setCellValue("")
                FileOutputStream(file).use { fos ->
                    workbook.write(fos)
                }
                workbook.close()
                readWorkbook(filePath)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 第一层：Workbook
     */
    fun getWorkbook(filePath: String): XSSFWorkbook? {
        val key: String = filePath
        if (hmWorkbook.containsKey(key)) {
            return hmWorkbook[key]
        }
        val workbook = readWorkbook(filePath) ?: return null
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

    fun getNumberOfSheets(workbook: XSSFWorkbook): Int {
        return workbook.numberOfSheets
    }

    fun getSheets(workbook: XSSFWorkbook): Iterator<Sheet> {
        return workbook.sheetIterator()
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

    fun getWorkbooks(): MutableCollection<XSSFWorkbook> {
        return hmWorkbook.values
    }
}