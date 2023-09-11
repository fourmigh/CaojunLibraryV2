package org.caojun.library.excel

import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream

object WriteExcelUtils {

    fun save(workbook: XSSFWorkbook, close: Boolean = false): String {
        return try {
            val filePath = ReadExcelUtils.getFilePath(workbook) ?: return ""
            val fos = FileOutputStream(filePath)
            workbook.write(fos)
            if (close) {
                workbook.close()
                fos.close()
                ReadExcelUtils.refresh(workbook)
            }
            filePath
        } catch (e: Exception) {
            ""
        }
    }

    fun createSheet(workbook: XSSFWorkbook): XSSFSheet {
        return workbook.createSheet()
    }

    fun shiftRows(sheet: XSSFSheet, startRow: Int = 0, n: Int = 1): Boolean {
        val endRow = sheet.lastRowNum + 1
        if (startRow >= endRow) {
            return true
        }
        return try {
            sheet.shiftRows(startRow, endRow, n)
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