package com.example.shoppinglistapp.ui.shoppinglist

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentShoppinglistBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ShoppinglistFragment : Fragment() {

    private var _binding: FragmentShoppinglistBinding? = null
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()

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


        appDb = AppDatabase.getDatabase(requireContext())

        lateinit var categories: List<Category>
        lateinit var items: List<Item>

        GlobalScope.launch {
            categories = appDb.categoryDao().getAll()

            if(categories.isNotEmpty())
            {
                withContext(Dispatchers.Main) {
                    if (categories != null)
                    {
                        categories.forEach {
                            items = appDb.itemDao().findByCategory(it.name!!)

                            addCategory(it.name)
                            for (item in items) {
                                addItem(item.name)
                            }
                        }
                    }
                }
            }
            else
            {
                withContext(Dispatchers.Main){
                    //toastMessage("Keine Daten gefunden")
                }
            }
        }



        return root
    }

    private fun addItem(name: String?) {

        val product_layout = getLayoutInflater().inflate(R.layout.product_linearlayout, null, false)
        val shoppinglist_layout = binding.layoutShoppingList

        product_layout.findViewById<TextView>(R.id.textView_product).text = name

        //get the checkbox to do something on checked
        product_layout.findViewById<CheckBox>(R.id.deletion_checkbox).setOnCheckedChangeListener {
                compoundButton, b -> Log.e("Checkbox", "Checkbox changed")

                shoppinglist_layout.removeView(product_layout)
        }

        shoppinglist_layout.addView(product_layout);
    }

    private fun addCategory(name: String) {

        val product_layout = getLayoutInflater().inflate(R.layout.category_headline_linearlayout, null, false)
        val shoppinglist_layout = binding.layoutShoppingList

        product_layout.findViewById<TextView>(R.id.textView_category).text = name

        shoppinglist_layout.addView(product_layout);
    }

    private fun removeItem(view: View, id: Int)
    {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}