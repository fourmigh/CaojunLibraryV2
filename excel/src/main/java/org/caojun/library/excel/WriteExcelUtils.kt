package org.caojun.library.excel

import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream

object WriteExcelUtils {

    fun save(workbook: XSSFWorkbook, filePath: String): Boolean {
        return try {
            val fos = FileOutputStream(filePath)
            workbook.write(fos)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun shiftRows(startRow: Int, sheet: XSSFSheet?, n: Int = 1): Boolean {
        if (startRow < 0 || sheet == null) {
            return false
        }
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

    fun write(sheet: XSSFSheet?, row: Int, column: Int, value: String): Boolean {
        if (sheet == null) {
            return false
        }
        return try {
            val newRow = if (row >= sheet.lastRowNum) {
                sheet.createRow(row)
            } else {
                sheet.getRow(row)
            }
            val cell = newRow.createCell(column)
            cell.setCellValue(value)
            true
        } catch (e: Exception) {
            false
        }
    }

//    fun insertCell(columnIndex: Int, rownum: Int, sheet: XSSFSheet?, value: String): Boolean {
//        if (sheet == null) {
//            return false
//        }
//        if (!shiftRows(rownum, sheet)) {
//            return false
//        }
//        return try {
//            val newRow = sheet.createRow(rownum)
//            newRow.createCell(columnIndex).setCellValue(value)
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    fun addCell(rownum: Int, sheet: XSSFSheet?, value: String): Boolean {
//        if (sheet == null) {
//            return false
//        }
//        return insertCell(sheet.lastRowNum + 1, rownum, sheet, value)
//    }
}