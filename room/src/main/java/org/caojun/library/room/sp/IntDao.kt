package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface IntDao {

    @get:Query("SELECT * FROM IntData")
    val all: List<IntData>?

    @Query("SELECT * FROM IntData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): IntData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: IntData)

    @Update
    fun update(data: IntData)

    @Delete
    fun delete(vararg data: IntData)

    @Query("DELETE FROM IntData")
    fun deleteAll()

    fun insertOrUpdate(data: IntData){
        val queryData = query(data.mKey)
        if (queryData == null){
            insert(data)
        }else{
            update(data)
        }
    }
}