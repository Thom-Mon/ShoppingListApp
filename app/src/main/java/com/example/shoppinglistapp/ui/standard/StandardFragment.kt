package com.example.shoppinglistapp.ui.standard

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentSettingsBinding
import com.example.shoppinglistapp.databinding.FragmentStandardBinding
import com.example.shoppinglistapp.ui.settings.SettingsViewModel

class StandardFragment : Fragment() {

    private var _binding: FragmentStandardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val standardViewModel =
            ViewModelProvider(this).get(StandardViewModel::class.java)

        _binding = FragmentStandardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textStandard
        standardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}