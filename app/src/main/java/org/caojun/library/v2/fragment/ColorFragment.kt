package org.caojun.library.v2.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import org.caojun.library.color.ColorUtils
import org.caojun.library.color.Colors
import org.caojun.library.v2.databinding.FragmentColorBinding

class ColorFragment : Fragment() {

    private var _binding: FragmentColorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentColorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val colors = Colors.values()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            colors
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spColors.adapter = adapter
        binding.spColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val index = binding.spColors.selectedItemPosition
                changeColor(colors[index].color)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.btnEnable.setOnClickListener {
            changeEnable(true)
        }

        binding.btnDisable.setOnClickListener {
            changeEnable(false)
        }

        return root
    }

    private fun changeEnable(enable: Boolean) {
        binding.btnButtonThumb.isEnabled = enable
        binding.btnSwitchThumb.isEnabled = enable
        binding.btnButtonBack.isEnabled = enable
        binding.btnSwitchBack.isEnabled = enable
    }

    private fun changeColor(color: Int) {
        val cslThumb = ColorUtils.generateThumbColorWithTintColor(color)
        binding.btnButtonThumb.setTextColor(cslThumb)
        binding.btnSwitchThumb.setTextColor(cslThumb)

        val cslBack = ColorUtils.generateBackColorWithTintColor(color)
        binding.btnButtonBack.setTextColor(cslBack)
        binding.btnSwitchBack.setTextColor(cslBack)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}