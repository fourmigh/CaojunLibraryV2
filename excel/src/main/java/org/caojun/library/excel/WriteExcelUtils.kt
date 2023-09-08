package org.caojun.library.excel

import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream

object WriteExcelUtils {

    fun save(workbook: XSSFWorkbook): Boolean {
        return try {
            val filePath = ReadExcelUtils.getFilePath(workbook) ?: return false
            val fos = FileOutputStream(filePath)
            workbook.write(fos)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shiftRows(sheet: XSSFSheet, startRow: Int, n: Int = 1): Boolean {
        if (startRow >= sheet.lastRowNum) {
            return true
        }
        return try {
            sheet.shiftRows(startRow, sheet.lastRowNum, n)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getWriteRow(sheet: XSSFSheet, rowIndex: Int): XSSFRow {
        return if (rowIndex >= sheet.lastRowNum) {
            sheet.createRow(rowIndex)
        } else {
            sheet.getRow(rowIndex)
        }
    }

    fun writeCell(row: XSSFRow, columnIndex: Int, value: String): Boolean {
        return try {
            val cell = row.createCell(columnIndex)
            cell.setCellValue(value)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun writeRow(row: XSSFRow, values: List<String>): Boolean {
        return try {
            for (columnIndex in values.indices) {
                val cell = row.createCell(columnIndex)
                cell.setCellValue(values[columnIndex])
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}