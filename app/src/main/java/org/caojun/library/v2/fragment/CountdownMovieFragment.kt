package org.caojun.library.v2.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.caojun.library.countdown.CountdownMovie
import org.caojun.library.v2.databinding.FragmentCountdownMovieBinding

class CountdownMovieFragment : Fragment() {

    private var _binding: FragmentCountdownMovieBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCountdownMovieBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.countdownMovie.start(10, object : CountdownMovie.Listener {
            override fun onFinish() {
                activity?.onBackPressed()
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}