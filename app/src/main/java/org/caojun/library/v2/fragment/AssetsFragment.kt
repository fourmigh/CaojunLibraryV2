package org.caojun.library.v2.fragment

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.caojun.library.assets.AssetsUtils
import org.caojun.library.gson.JsonUtils
import org.caojun.library.kotlin.runThread
import org.caojun.library.kotlin.runUiThread
import org.caojun.library.v2.databinding.FragmentAssetsBinding

class AssetsFragment : Fragment() {

    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAssetsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val context = requireContext()
        binding.btnReadByteArray.setOnClickListener {
            runThread {
                val bytes = AssetsUtils.readByteArray(context, "CHINA1.DKL")
                runUiThread {
                    binding.tvAssets.text = JsonUtils.toJson(bytes?.toMutableList())
                }
            }
        }

        binding.btnReadListString.setOnClickListener {
            runThread {
                val list = AssetsUtils.readListString(context, "TRI001-63F55C2C_1524.XML")
                runUiThread {
                    binding.tvAssets.text = JsonUtils.toJson(list)
                }
            }
        }

        binding.btnLoadBitmap.setOnClickListener {
            runThread {
                val bitmap = AssetsUtils.loadBitmap(context, "test2.BMP")
                runUiThread {
                    binding.ivAssets.setImageBitmap(bitmap)
                }
            }
        }

        binding.tvAssets.movementMethod = ScrollingMovementMethod.getInstance()
        binding.tvAssets.setOnClickListener {
            binding.tvAssets.text = null
        }

        binding.ivAssets.setOnClickListener {
            binding.ivAssets.setImageBitmap(null)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}