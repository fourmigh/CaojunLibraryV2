package org.caojun.library.kotlin

import android.os.Handler
import android.os.Looper

fun runThread(method: () -> Unit) {
    object : Thread() {
        override fun run() {
            method()
        }
    }.start()
}

private val handler = Handler(Looper.getMainLooper())
fun runUiThread(method: () -> Unit) {
    handler.post {
        method()
    }
}