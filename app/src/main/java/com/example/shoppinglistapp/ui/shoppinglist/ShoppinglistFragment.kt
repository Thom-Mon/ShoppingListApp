package com.example.shoppinglistapp.ui.shoppinglist

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentGalleryBinding
import com.example.shoppinglistapp.databinding.FragmentShoppinglistBinding
import com.example.shoppinglistapp.ui.gallery.GalleryViewModel

class ShoppinglistFragment : Fragment() {

    private var _binding: FragmentShoppinglistBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val shoppinglistViewModel =
            ViewModelProvider(this).get(ShoppinglistViewModel::class.java)

        _binding = FragmentShoppinglistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textShopping
        shoppinglistViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}