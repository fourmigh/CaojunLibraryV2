package org.caojun.library.v2.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.caojun.library.currency.ChineseNumberUtils
import org.caojun.library.currency.Currency
import org.caojun.library.v2.databinding.FragmentCurrencyBinding

class CurrencyFragment : Fragment() {

    private var _binding: FragmentCurrencyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val countryCodes = ArrayList<String>()
        for (countryCode in Currency.numbers) {
            countryCodes.add(countryCode.toString())
        }
        val adapterCountryCode = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            countryCodes
        )
        adapterCountryCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNumber.adapter = adapterCountryCode
        binding.spNumber.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                parse()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.etCurrency.addTextChangedListener {
            parse()
        }

        return root
    }

    private fun parse() {
        val text = binding.etCurrency.text.toString()
        if (TextUtils.isEmpty(text)) {
            return
        }
        val index = binding.spNumber.selectedItemPosition
        val number = Currency.numbers[index]
        val fen = (text.toFloat() * 100).toInt()
        val currency = Currency.displayName(number)

        val amount = Currency.formatAmount(fen, number)
        val chineseNumber = ChineseNumberUtils.numToChinese(text, false, false)
        val chineseAmount = ChineseNumberUtils.numToChinese(text, true, true)

        binding.tvCurrency.text = currency
        binding.tvAmount.text = amount
        binding.tvChineseNumber.text = chineseNumber
        binding.tvCapital.text = chineseAmount
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}