package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface ByteDao {

    @get:Query("SELECT * FROM ByteData")
    val all: List<ByteData>?

    @Query("SELECT * FROM ByteData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): ByteData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: ByteData)

    @Update
    fun update(data: ByteData)

    @Delete
    fun delete(vararg data: ByteData)

    @Query("DELETE FROM ByteData")
    fun deleteAll()

    fun insertOrUpdate(data: ByteData){
        val queryData = query(data.mKey)
        if (queryData == null){
            insert(data)
        }else{
            update(data)
        }
    }
}