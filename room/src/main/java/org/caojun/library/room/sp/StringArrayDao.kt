package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface StringArrayDao {

    @get:Query("SELECT * FROM StringArrayData")
    val all: List<StringArrayData>?

    @Query("SELECT * FROM StringArrayData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): StringArrayData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: StringArrayData)

    @Delete
    fun delete(vararg data: StringArrayData)

    @Query("DELETE FROM StringArrayData")
    fun deleteAll()
}