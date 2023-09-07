package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface FloatArrayDao {

    @get:Query("SELECT * FROM FloatArrayData")
    val all: List<FloatArrayData>?

    @Query("SELECT * FROM FloatArrayData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): FloatArrayData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: FloatArrayData)

    @Delete
    fun delete(vararg data: FloatArrayData)

    @Query("DELETE FROM FloatArrayData")
    fun deleteAll()
}