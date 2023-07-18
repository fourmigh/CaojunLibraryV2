package org.caojun.library.countdown

import android.text.TextUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CountdownManager {

    companion object {
        private var countdownManager: CountdownManager? = null

        @JvmStatic
        fun getInstance(): CountdownManager {
            if (countdownManager == null) {
                synchronized(CountdownManager::class.java) {
                    if (countdownManager == null) {
                        countdownManager = CountdownManager()
                    }
                }
            }
            return countdownManager!!
        }
    }

    interface Listener {
        fun onCuckoo(key: String)
    }

    //    inner class Counter {
//        var key = ""
//        //计数器
//        var count = 0
//        //倒计时时间
//        var time = 0L
//    }
    inner class Counter(private val time: Int) {
        private var count = time
        var isPausing = false
        var lastTime = 0L
        var currentTime = 0L

        fun doCount(): Boolean {
            if (isPausing) {
                return false
            }
            count --
            if (count <= 0) {
                count = time

                lastTime = currentTime
                currentTime = System.currentTimeMillis()
                return true
            }
            return false
        }

        fun reset(){
            count = time
        }
    }

    private val counters = ConcurrentHashMap<String, Counter>()
    private val listeners = ConcurrentHashMap<String, Listener>()

    private var timer: Timer? = null

    fun addListener(key: String, listener: Listener): Boolean {
        return addListener(key, listener, 1)
    }
    fun addListener(key: String, listener: Listener, time: Int = 1): Boolean {
        if (TextUtils.isEmpty(key) || time <= 0) {
            return false
        }
        val counter = Counter(time)
        counters[key] = counter
        listeners[key] = listener
        start()
        return true
    }

    fun removeListener(key: String): Boolean {
        if (listeners.containsKey(key) && counters.containsKey(key)) {
            listeners.remove(key)
            counters.remove(key)
            return true
        }
        return false
    }

    fun pause(key: String) {
        pauseOrResume(key, true)
    }

    fun resume(key: String) {
        pauseOrResume(key, false)
    }

    fun pauseExcept(key: String) {
        pauseOrResumeExcept(key, true)
    }

    fun resumeExcept(key: String) {
        pauseOrResumeExcept(key, false)
    }

    fun getCounter(key: String): Counter? {
        return counters[key]
    }

    private fun pauseOrResume(key: String, isPausing: Boolean) {
        if (counters.containsKey(key)) {
            val counter = counters[key]
            if (counter != null) {
                counter.isPausing = isPausing
            }
        }
    }

    private fun pauseOrResumeExcept(key: String, isPausing: Boolean) {
        for (counter in counters) {
            if (counter.key == key) {
                continue
            }
            counter.value.isPausing = isPausing
        }
    }

    @Synchronized
    fun start() {
        if (timer == null) {
            timer = Timer()
        } else {
            return
        }
        timerSchedule()
    }

    fun stop() {
        timer?.cancel()
        timer = null
        counters.clear()
        listeners.clear()
    }

    fun timerSchedule() {
        timer?.schedule(object : TimerTask() {
            override fun run() {
                for (key in counters.keys) {
                    val counter = counters[key]
                    val listener = listeners[key]
                    if (counter == null || listener == null) {
                        continue
                    }
                    if (counter.doCount()) {
                        listener.onCuckoo(key)
                    }
                }
            }
        }, 0, 1000)
    }

    private fun pauseOrResumeAll(isPausing: Boolean) {
        for (counter in counters.values) {
            counter.isPausing = isPausing
        }
    }

    fun pauseAll() {

        pauseOrResumeAll(true)
    }

    fun resumeAll() {

        pauseOrResumeAll(false)
    }

    fun resetAll() {

        for (counter in counters.values) {
            counter.reset()
        }
    }
}