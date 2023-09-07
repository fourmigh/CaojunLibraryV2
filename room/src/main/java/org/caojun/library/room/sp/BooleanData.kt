package org.caojun.library.room.sp

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class BooleanData {

    @NonNull
    @PrimaryKey
    var mKey = ""

    var mValue = false
}