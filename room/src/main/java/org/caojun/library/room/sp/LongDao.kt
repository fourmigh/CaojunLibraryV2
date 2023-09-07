package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface LongDao {

    @get:Query("SELECT * FROM LongData")
    val all: List<LongData>?

    @Query("SELECT * FROM LongData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): LongData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: LongData)

    @Update
    fun update(data: LongData)

    @Delete
    fun delete(vararg data: LongData)

    @Query("DELETE FROM LongData")
    fun deleteAll()

    fun insertOrUpdate(data: LongData){
        val queryData = query(data.mKey)
        if (queryData == null){
            insert(data)
        }else{
            update(data)
        }
    }
}