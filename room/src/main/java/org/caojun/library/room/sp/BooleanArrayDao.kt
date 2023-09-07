package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface BooleanArrayDao {

    @get:Query("SELECT * FROM BooleanArrayData")
    val all: List<BooleanArrayData>?

    @Query("SELECT * FROM BooleanArrayData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): BooleanArrayData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: BooleanArrayData)

    @Delete
    fun delete(vararg data: BooleanArrayData)

    @Query("DELETE FROM BooleanArrayData")
    fun deleteAll()
}