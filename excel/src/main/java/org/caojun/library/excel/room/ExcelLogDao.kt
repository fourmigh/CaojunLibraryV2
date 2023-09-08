package org.caojun.library.excel.room

import androidx.room.*

@Dao
interface ExcelLogDao {

    @Query("SELECT * FROM ExcelLog")
    fun queryAll(): List<ExcelLog>

    @Query("SELECT * FROM ExcelLog ORDER BY id ASC LIMIT 1")
    fun queryFirst(): ExcelLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg excelLog: ExcelLog)

    @Delete
    fun delete(vararg excelLog: ExcelLog)

    @Update
    fun update(vararg excelLog: ExcelLog)
}