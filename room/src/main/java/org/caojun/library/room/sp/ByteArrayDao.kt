package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface ByteArrayDao {

    @get:Query("SELECT * FROM ByteArrayData")
    val all: List<ByteArrayData>?

    @Query("SELECT * FROM ByteArrayData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): ByteArrayData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: ByteArrayData)

    @Delete
    fun delete(vararg data: ByteArrayData)

    @Query("DELETE FROM ByteArrayData")
    fun deleteAll()
}