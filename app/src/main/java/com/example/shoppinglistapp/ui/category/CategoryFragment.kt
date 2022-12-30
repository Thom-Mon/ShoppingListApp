package com.example.shoppinglistapp.ui.category

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.adapter.CustomAdapter
import com.example.shoppinglistapp.adapter.ElementsViewModel
import com.example.shoppinglistapp.databinding.FragmentCategoryBinding
import com.example.shoppinglistapp.databinding.FragmentShoppinglistBinding
import com.example.shoppinglistapp.ui.shoppinglist.ShoppinglistViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private  lateinit var appDb : AppDatabase
    private val gson = Gson()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var data = ArrayList<ElementsViewModel>()
    private val adapter = CustomAdapter(data)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val categoryViewModel =
            ViewModelProvider(this).get(CategoryViewModel::class.java)

        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textCategory
        categoryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        // code here

        appDb = AppDatabase.getDatabase(requireContext())

        val recyclerView = binding.recyclerviewCategory
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = adapter


        lateinit var entries: List<Category>

        // Create here the setWhenClickListener für den Adapter
        GlobalScope.launch {
            entries = appDb.categoryDao().getAll()

            if(entries.isNotEmpty())
            {
                withContext(Dispatchers.Main) {
                    if (entries != null) {
                        entries.forEach {
                            data.add(
                                ElementsViewModel(SpannableStringBuilder(it.name).toString())
                                    )
                        }
                        adapter.notifyDataSetChanged()
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
        //END



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}