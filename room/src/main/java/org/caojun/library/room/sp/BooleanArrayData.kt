package org.caojun.library.room.sp

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(BooleanArrayConverter::class)
class BooleanArrayData {

    @NonNull
    @PrimaryKey
    var mKey = ""

    var mValue = ArrayList<Boolean>()
}