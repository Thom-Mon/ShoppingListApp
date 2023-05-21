package com.example.shoppinglistapp.ui.standard

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import com.example.shoppinglistapp.AppDatabase
import com.example.shoppinglistapp.Dao.Category.Category
import com.example.shoppinglistapp.Dao.Item.Item
import com.example.shoppinglistapp.R
import com.example.shoppinglistapp.databinding.FragmentSettingsBinding
import com.example.shoppinglistapp.databinding.FragmentStandardBinding
import com.example.shoppinglistapp.showConfirmationDialog
import com.example.shoppinglistapp.ui.settings.SettingsViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StandardFragment : Fragment() {

    private var _binding: FragmentStandardBinding? = null
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
        val standardViewModel =
            ViewModelProvider(this).get(StandardViewModel::class.java)

        _binding = FragmentStandardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        appDb = AppDatabase.getDatabase(requireContext())

        lateinit var categories: List<Category>
        lateinit var items: List<Item>

        // fill the standards like the shopping list on startup
        GlobalScope.launch {
            categories = appDb.categoryDao().getAllNotDeleted()

            if(categories.isNotEmpty())
            {
                withContext(Dispatchers.Main) {
                    if (categories != null)
                    {
                        categories.forEach {
                            items = appDb.itemDao().findByCategoryWithStatus(it.name!!,1)

                            addCategory(it.name)
                            for (item in items) {
                                addItem(item)
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

    private fun addItem(item: Item) {
        if(item.id == null){
            Log.i("Button", "Item Id is null")
            return
        }

        val product_layout = getLayoutInflater().inflate(R.layout.product_standard_linearlayout, null, false)
        val standardlist_layout = binding.layoutStandardlist

        product_layout.findViewById<TextView>(R.id.textView_product).text = item.name

        //get the checkbox to do something on checked
        product_layout.findViewById<ImageButton>(R.id.button_reuse_item).setOnClickListener {
            updateItem(product_layout, item.id!!)

            Log.e("Reuse", "Id: " + item.id!!)
        }

        product_layout.findViewById<ImageButton>(R.id.button_delete_item).setOnClickListener {
            showConfirmationDialog("Produkt löschen", "Wollen Sie das Produkt wirklich endgültig löschen?")
            {
                deleteItem(product_layout, item.id!!)
            }
        }

        standardlist_layout.addView(product_layout);
    }

    private fun addCategory(name: String) {

        val category_layout = getLayoutInflater().inflate(R.layout.category_headline_linearlayout, null, false)
        val standardlist_layout = binding.layoutStandardlist

        category_layout.findViewById<TextView>(R.id.textView_category).text = name

        standardlist_layout.addView(category_layout);
    }

    private fun updateItem(view: View, id: Int )
    {
        val animationFadeOut = AnimationUtils.loadAnimation(context, R.anim.item_animation_zoom)
        view.startAnimation(animationFadeOut)
        Handler().postDelayed({
            binding.layoutStandardlist.removeView(view)
        }, 300)

        GlobalScope.launch {
            appDb.itemDao().updateStatus(id,0)
            //deleteData(elementsViewModel.id.toInt())
        }
    }

    private fun deleteItem(view: View, id: Int )
    {
        val animationFadeOut = AnimationUtils.loadAnimation(context, R.anim.item_animation_zoom)
        view.startAnimation(animationFadeOut)
        Handler().postDelayed({
            binding.layoutStandardlist.removeView(view)
        }, 300)
        lateinit var item: Item

        GlobalScope.launch {
            item  = appDb.itemDao().findById(id)
            appDb.itemDao().softDelete(item.id!!)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}