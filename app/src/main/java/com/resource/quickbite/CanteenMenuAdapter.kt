package com.resource.quickbite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class CanteenMenuAdapter(options: FirebaseRecyclerOptions<MenuItem>) :
    FirebaseRecyclerAdapter<MenuItem, CanteenMenuAdapter.MenuViewHolder>(options) {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_canteen_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int, model: MenuItem) {
        val ref = getRef(position)


        holder.btnEdit.setOnClickListener {
            // 🔹 (You can add dialog here to edit food name & cost)
        }
    }
}
