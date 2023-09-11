package org.caojun.library.v2.fragment

import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.caojun.library.excel.ExcelManager
import org.caojun.library.excel.enums.SaveType
import org.caojun.library.excel.XlsLog
import org.caojun.library.excel.enums.Order
import org.caojun.library.excel.room.ExcelLog
import org.caojun.library.klog.KLog
import org.caojun.library.kotlin.runThread
import org.caojun.library.kotlin.runUiThread
import org.caojun.library.v2.databinding.FragmentXlslogBinding

class XlsLogFragment : Fragment() {

    private var _binding: FragmentXlslogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentXlslogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val context = requireContext()

        binding.btnLogStart.setOnClickListener {
            XlsLog.init(SaveType.ROOM, getOrder(), 100)
            XlsLog.start(this.requireActivity().applicationContext)
            start()
        }

        binding.btnLogStop.setOnClickListener {
            XlsLog.stop()
            stop()
        }

        binding.btnOpenExcel.setOnClickListener {
            val fileName = binding.etFileName.text.toString()
            if (TextUtils.isEmpty(fileName)) {
                return@setOnClickListener
            }
            binding.btnOpenExcel.isEnabled = false
            runThread {
                val workbook = ExcelManager.getInstance().openFile(context, fileName)
                workbook?.close()
                runUiThread {
                    binding.btnOpenExcel.isEnabled = true
                }
            }
        }

        binding.btnLogExport.setOnClickListener {
            binding.btnLogExport.isEnabled = false
            XlsLog.init(SaveType.ROOM, getOrder(), 500)
            XlsLog.exportToExcel(context, object : XlsLog.ExportListener {
                override fun onDataRead(excelLog: ExcelLog, count: Int): Pair<String?, String?> {
                    KLog.i("XlsLog.ExportListener", "onDataRead[$count]: ${excelLog.getCells()}")
                    return Pair(excelLog.getDate(), null)
                }

                override fun onDataWrite(cells: List<String>, filePath: String) {
                    KLog.i("XlsLog.ExportListener", "onDataWrite: $filePath")
                }

                override fun onFinish() {
                    KLog.i("XlsLog.ExportListener", "onFinish")
                    runUiThread {
                        binding.btnLogExport.isEnabled = true
                    }
                }
            })
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getOrder(): Order {
        if (binding.rbASC.isChecked) {
            return Order.ASC
        }
        if (binding.rbDESC.isChecked) {
            return Order.DESC
        }
        return Order.DESC
    }

    private var isRunning = false
    private fun start() {
        if (isRunning) {
            return
        }
        isRunning = true
        runThread {
            while (isRunning) {
                XlsLog.d("XlsLog", "test: ${System.currentTimeMillis()}")
                SystemClock.sleep(1)
            }
        }
    }
    private fun stop() {
        isRunning = false
    }
}