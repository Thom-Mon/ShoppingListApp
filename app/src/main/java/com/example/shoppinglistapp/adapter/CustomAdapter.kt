package com.example.shoppinglistapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglistapp.R
import java.security.AccessController.getContext

class CustomAdapter(private val mList: List<ElementsViewModel>): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var listener: OnItemsClickListener? = null

    interface OnItemsClickListener {
        fun onItemClick(elementsViewModel: ElementsViewModel, buttonId: Int, filename: String)
    }

    fun setWhenClickListener(listener: OnItemsClickListener?) {
        Log.e("Button", "Function Called in Adapter")
        this.listener = listener
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false) // here put the layout inside of the design of the cardview

        return ViewHolder(view)
    }


    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val elementsViewModel = mList[position]

        // sets the text to the textview from our itemHolder class


        holder.deleteCard.setOnClickListener(View.OnClickListener {
            Log.e("Mark_ (holder.deleteCard) listener: ", elementsViewModel.name)
            if (listener != null) {
                listener!!.onItemClick(elementsViewModel, 0,elementsViewModel.name)
            }
        })

        holder.textView.text = elementsViewModel.name

        holder.textView.setOnClickListener(View.OnClickListener {
            Log.e("Mark_ on Name click", elementsViewModel.name)
            if (listener != null) {
                listener!!.onItemClick(elementsViewModel, 1,elementsViewModel.name)
            }
        })
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = itemView.findViewById(R.id.card_category_name)
        val deleteCard: ImageView = itemView.findViewById(R.id.delete_on_card_view)
    }
}
