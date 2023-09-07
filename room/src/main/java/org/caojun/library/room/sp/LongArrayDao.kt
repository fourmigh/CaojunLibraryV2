package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface LongArrayDao {

    @get:Query("SELECT * FROM LongArrayData")
    val all: List<LongArrayData>?

    @Query("SELECT * FROM LongArrayData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): LongArrayData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: LongArrayData)

    @Delete
    fun delete(vararg data: LongArrayData)

    @Query("DELETE FROM LongArrayData")
    fun deleteAll()
}