package org.caojun.library.v2.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import org.caojun.library.timer.TimeZoneUtils
import org.caojun.library.v2.databinding.FragmentTimeBinding

class TimeFragment : Fragment() {

    private var _binding: FragmentTimeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTimeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val context = requireContext()

        val timezones = TimeZoneUtils.getTimeZones()
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_activated_1, timezones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTimeZones.adapter = adapter

        val timezones2 = TimeZoneUtils.getTimeZones2()
        val adapterLeft = ArrayAdapter(context, android.R.layout.simple_list_item_activated_1, timezones2.first)
        adapterLeft.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTimeZoneLeft.adapter = adapterLeft
        binding.spTimeZoneLeft.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val timezones2Right = timezones2.second[timezones2.first[binding.spTimeZoneLeft.selectedItemPosition]]
                if (timezones2Right != null) {
                    val adapterRight = ArrayAdapter(
                        context,
                        android.R.layout.simple_list_item_activated_1,
                        timezones2Right.toList()
                    )
                    adapterRight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spTimeZoneRight.adapter = adapterRight

                    freshTimeZoneTextView()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.spTimeZoneRight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                freshTimeZoneTextView()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        return root
    }

    private fun freshTimeZoneTextView() {
        val leftIndex = binding.spTimeZoneLeft.selectedItemPosition
        val rightIndex = binding.spTimeZoneRight.selectedItemPosition
        binding.tvTimeZone.text = TimeZoneUtils.getTimeZone(leftIndex, rightIndex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}