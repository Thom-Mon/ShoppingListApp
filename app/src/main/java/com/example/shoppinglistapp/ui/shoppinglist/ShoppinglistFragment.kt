package com.example.shoppinglistapp.ui.shoppinglist

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentShoppinglistBinding


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

        buildShoppingList()

        return root
    }

    private fun buildShoppingList() {
        val layout = binding.layoutShoppingList

        // add dynamically to shoppingList
        val button = Button(context)

        // setting layout_width and layout_height using layout parameters
        button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        button.text = "Kategorie"

        // add Button to LinearLayout
        layout.addView(button)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}