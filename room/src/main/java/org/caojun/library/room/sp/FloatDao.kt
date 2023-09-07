package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface FloatDao {

    @get:Query("SELECT * FROM FloatData")
    val all: List<FloatData>?

    @Query("SELECT * FROM FloatData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): FloatData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: FloatData)

    @Update
    fun update(data: FloatData)

    @Delete
    fun delete(vararg data: FloatData)

    @Query("DELETE FROM FloatData")
    fun deleteAll()

    fun insertOrUpdate(data: FloatData){
        val queryData = query(data.mKey)
        if (queryData == null){
            insert(data)
        }else{
            update(data)
        }
    }
}