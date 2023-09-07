package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface StringDao {

    @get:Query("SELECT * FROM StringData")
    val all: List<StringData>?

    @Query("SELECT * FROM StringData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): StringData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: StringData)

    @Update
    fun update(data: StringData)

    @Delete
    fun delete(vararg data: StringData)

    @Query("DELETE FROM StringData")
    fun deleteAll()

    fun insertOrUpdate(data: StringData){
        val queryData = query(data.mKey)
        if (queryData == null){
            insert(data)
        }else{
            update(data)
        }
    }
}