package org.caojun.library.room.sp

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class FloatData {

    @NonNull
    @PrimaryKey
    var mKey = ""

    var mValue = 0F
}