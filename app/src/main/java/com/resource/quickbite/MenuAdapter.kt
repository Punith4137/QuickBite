package com.resource.quickbite

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter(
    private val items: List<MenuItem>,
    private val onItemSelected: (MenuItem, Boolean) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private val selectedItems = mutableSetOf<MenuItem>()

    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardItem)
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.itemName
        holder.tvPrice.text = "₹${item.itemPrice}"

        val isSelected = selectedItems.contains(item)

        // ✅ Explicitly set background color (prevents white reset bug)
        holder.card.setCardBackgroundColor(
            if (isSelected) Color.parseColor("#91FF91")  // Green when selected
            else Color.parseColor("#FFFFFF")            // Yellow default
        )

        holder.card.setOnClickListener {
            val currentlySelected = selectedItems.contains(item)
            if (currentlySelected) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }

            onItemSelected(item, !currentlySelected)
        }
    }

    override fun getItemCount() = items.size
}
