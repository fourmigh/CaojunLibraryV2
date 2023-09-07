package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface IntArrayDao {

    @get:Query("SELECT * FROM IntArrayData")
    val all: List<IntArrayData>?

    @Query("SELECT * FROM IntArrayData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): IntArrayData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: IntArrayData)

    @Delete
    fun delete(vararg data: IntArrayData)

    @Query("DELETE FROM IntArrayData")
    fun deleteAll()
}