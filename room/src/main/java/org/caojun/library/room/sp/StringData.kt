package org.caojun.library.room.sp

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class StringData {

    @NonNull
    @PrimaryKey
    var mKey = ""

    var mValue = ""
}