package org.caojun.library.excel

import android.content.Context
import android.os.SystemClock
import org.caojun.library.excel.enums.Order
import org.caojun.library.excel.enums.SaveType
import org.caojun.library.excel.room.ExcelLog
import org.caojun.library.excel.room.ExcelLogDatabase
import org.caojun.library.excel.room.LogType
import org.caojun.library.klog.KLog
import org.caojun.library.kotlin.runThread

object XlsLog {

    private var saveType = SaveType.ROOM
    private var order = Order.DESC
    private var maxRowsPerSheet = 1000

    fun init(saveType: SaveType, order: Order = Order.DESC, maxRowsPerSheet: Int = 1000) {
        XlsLog.saveType = saveType
        XlsLog.order = order
        XlsLog.maxRowsPerSheet = maxRowsPerSheet
    }

    fun v(tag: String, log: String) {
        addLog(LogType.V, tag, log)
        KLog.v(tag, log)
    }

    fun d(tag: String, log: String) {
        addLog(LogType.D, tag, log)
        KLog.d(tag, log)
    }

    fun i(tag: String, log: String) {
        addLog(LogType.I, tag, log)
        KLog.i(tag, log)
    }

    fun w(tag: String, log: String) {
        addLog(LogType.W, tag, log)
        KLog.w(tag, log)
    }

    fun e(tag: String, log: String) {
        addLog(LogType.E, tag, log)
        KLog.e(tag, log)
    }

    fun a(tag: String, log: String) {
        addLog(LogType.A, tag, log)
        KLog.a(tag, log)
    }

    @Synchronized
    private fun addLog(type: LogType, tag: String, log: String, stackTraceIndex: Int = 5) {
        val excelLog = ExcelLog()
        excelLog.type = type
        excelLog.tag = tag

        ////////////////////////////////////////////////////////////////////////////////////////////
        val stackTrace = Thread.currentThread().stackTrace
        val targetElement = stackTrace[stackTraceIndex]
        var className = targetElement.className
        val fileName = targetElement.fileName
        val suffix = fileName.substring(fileName.lastIndexOf("."))
        val classNameInfo = className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (classNameInfo.isNotEmpty()) {
            className = classNameInfo[classNameInfo.size - 1] + suffix
        }
        if (className.contains("$")) {
            className = className.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + suffix
        }
        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber
        if (lineNumber < 0) {
            lineNumber = 0
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        excelLog.className = className
        excelLog.lineNumber = lineNumber
        excelLog.methodName = methodName
        excelLog.log = log
        logs.add(excelLog)
    }

    private fun saveLog(context: Context) {
        if (logs.isEmpty()) {
            SystemClock.sleep(1000)
            return
        }
        doSaveLog(context)
        SystemClock.sleep(10)
    }

    @Synchronized
    private fun doSaveLog(context: Context) {
        try {
            val excelLog = logs[0]
            when (saveType) {
                SaveType.ROOM -> {
                    saveToDatabase(context, excelLog)
                }
                SaveType.XLSX -> {
                    insert(context, excelLog)
                }
            }
            logs.removeAt(0)
            KLog.i("XlsLog", "saveLog.excelLog: ${excelLog.getCells()}")
            KLog.d("XlsLog", "saveLog.left: ${logs.size}")
        } catch (e: Exception) {
            KLog.e("XlsLog", "saveLog: $e")
        }
    }

    private fun saveToDatabase(context: Context, excelLog: ExcelLog) {
        val dao = ExcelLogDatabase.getDatabase(context).getDao()
        dao.insert(excelLog)
    }

    private fun insert(context: Context, excelLog: ExcelLog) {
        val date = excelLog.getDate()
        val workbook = ExcelManager.getInstance().openFile(context, date) ?: return
        when (order) {
            Order.DESC -> {
                ExcelManager.getInstance().insert(workbook, excelLog.getCells(), maxRowsPerSheet)
            }
            Order.ASC -> {
                ExcelManager.getInstance().add(workbook, excelLog.getCells(), maxRowsPerSheet)
            }
        }
    }

    private val logs = ArrayList<ExcelLog>()
    private var isRunning = false

    fun start(context: Context): Boolean {
        if (isRunning || logs.isNotEmpty()) {
            return false
        }
        isRunning = true
        runThread {
            while (isRunning || logs.isNotEmpty()) {
                saveLog(context)
            }

            ExcelManager.getInstance().saveFile()
        }
        return true
    }

    fun stop() {
        isRunning = false
    }
}