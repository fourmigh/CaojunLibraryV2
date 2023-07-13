package org.caojun.library.v2.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.caojun.library.kotlin.runThread
import org.caojun.library.kotlin.runUiThread
import org.caojun.library.pinyin.PinyinUtils
import org.caojun.library.v2.databinding.FragmentPinyinBinding

class PinyinFragment : Fragment() {

    private var _binding: FragmentPinyinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPinyinBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.tvPinyin.setOnClickListener {
            binding.tvPinyin.text = null
        }
        binding.tvFirstLetters.setOnClickListener {
            binding.tvFirstLetters.text = null
        }

        binding.etPinyin.addTextChangedListener {
            binding.btnPinyin.isEnabled = !TextUtils.isEmpty(it?.toString())
        }
        binding.btnPinyin.setOnClickListener {
            runThread {
                val text = binding.etPinyin.text.toString()
                val isLowercase = binding.swLowercase.isChecked
                val pinyin = PinyinUtils.toPinyin(text, " ", isLowercase)
                val firstLetter = PinyinUtils.firstLetters(text, isLowercase)

                runUiThread {
                    binding.tvPinyin.text = pinyin
                    binding.tvFirstLetters.text = firstLetter
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}