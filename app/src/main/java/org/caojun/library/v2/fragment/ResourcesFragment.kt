package org.caojun.library.v2.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import org.caojun.library.resources.LocaleUtils
import org.caojun.library.timer.TimeUtils
import org.caojun.library.timer.addLog
import org.caojun.library.v2.databinding.FragmentResourcesBinding

class ResourcesFragment : Fragment() {

    private var _binding: FragmentResourcesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentResourcesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val context = requireContext()

        val locales = LocaleUtils.getLocales()
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_activated_1, locales)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spLanguage.adapter = adapter
        binding.spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                LocaleUtils.setLocale(context, locales[binding.spLanguage.selectedItemPosition])
                binding.tvLanguage.addLog("resourceName:    ${context.getString(org.caojun.library.timer.R.string.resource_name)}")

                val defaultLocal = LocaleUtils.getLocale()
                binding.tvLanguage.addLog("defaultLocal:    $defaultLocal")

                val dataFormat = context.getString(org.caojun.library.timer.R.string.data_format)
                val time = TimeUtils.getTime(dataFormat)
                binding.tvLanguage.addLog("time:    $time")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}