package org.caojun.library.room.sp

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(LongArrayConverter::class)
class LongArrayData {

    @NonNull
    @PrimaryKey
    var mKey = ""

    var mValue = ArrayList<Long>()
}