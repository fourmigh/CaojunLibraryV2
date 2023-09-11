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
            val result = when (saveType) {
                SaveType.ROOM -> {
                    saveToDatabase(context, excelLog)
                }
                SaveType.XLSX -> {
                    saveToExcel(context, excelLog)
                }
            }
            if (result) {
                logs.removeAt(0)
                KLog.i("XlsLog", "saveLog.excelLog: ${excelLog.getCells()}")
                KLog.d("XlsLog", "saveLog.left: ${logs.size}")
            }
        } catch (e: Exception) {
            KLog.e("XlsLog", "saveLog: $e")
        }
    }

    private fun saveToDatabase(context: Context, excelLog: ExcelLog): Boolean {
        return try {
            val dao = ExcelLogDatabase.getDatabase(context).getDao()
            dao.insert(excelLog)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveToExcel(context: Context, excelLog: ExcelLog): Boolean {
        val date = excelLog.getDate()
        val workbook = ExcelManager.getInstance().openFile(context, date) ?: return false
        return ExcelManager.getInstance().writeRow(workbook, excelLog.getCells(), order, maxRowsPerSheet)
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

            ExcelManager.getInstance().saveAllFilesAndClose()
        }
        return true
    }

    fun stop() {
        isRunning = false
    }

    interface ExportListener {
        /**
         * first: fileName
         * second: folderName
         */
        fun onDataRead(excelLog: ExcelLog, left: Int): Pair<String?, String?>
        fun onDataWrite(cells: List<String>, filePath: String)
        fun onFinish()
    }
    /**
     * 从数据库导出到Excel
     */
    fun exportToExcel(context: Context, listener: ExportListener? = null) {
        runThread {
            val dao = ExcelLogDatabase.getDatabase(context).getDao()
            var excelLog = dao.queryFirst()
            while (excelLog != null) {
                val count = dao.count()
                val pair = listener?.onDataRead(excelLog, count)
                val fileName = pair?.first ?: excelLog.getDate()
                val folderName = pair?.second ?: ExcelManager.FOLDER_NAME
                val workbook = ExcelManager.getInstance().openFile(context, fileName, folderName) ?: return@runThread
                ExcelManager.getInstance().writeRow(workbook, excelLog.getCells(), order, maxRowsPerSheet)
                val filePath = WriteExcelUtils.save(workbook, true)
                listener?.onDataWrite(excelLog.getCells(), filePath)

                dao.delete(excelLog)
                excelLog = dao.queryFirst()
            }
            listener?.onFinish()
        }
    }
}