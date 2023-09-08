package org.caojun.library.v2.fragment

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.caojun.library.excel.XlsLog
import org.caojun.library.kotlin.runThread
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

//        val context = requireContext()

        binding.btnLogStart.setOnClickListener {
            XlsLog.start(this.requireActivity().applicationContext)
            start()
        }

        binding.btnLogStop.setOnClickListener {
            XlsLog.stop()
            stop()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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