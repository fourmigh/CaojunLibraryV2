package org.caojun.library.room.sp

import androidx.room.*

@Dao
interface BooleanDao {

    @get:Query("SELECT * FROM BooleanData")
    val all: List<BooleanData>?

    @Query("SELECT * FROM BooleanData WHERE mKey = :mKey LIMIT 1")
    fun query(mKey: String): BooleanData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: BooleanData)

    @Update
    fun update(data: BooleanData)

    @Delete
    fun delete(vararg data: BooleanData)

    @Query("DELETE FROM BooleanData")
    fun deleteAll()

    fun insertOrUpdate(data: BooleanData){
        val queryData = query(data.mKey)
        if (queryData == null){
            insert(data)
        }else{
            update(data)
        }
    }
}