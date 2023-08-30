package org.caojun.library.v2.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.caojun.library.v2.databinding.FragmentToastBinding

class ToastFragment : Fragment() {

    private var _binding: FragmentToastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentToastBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val context = requireContext()



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}