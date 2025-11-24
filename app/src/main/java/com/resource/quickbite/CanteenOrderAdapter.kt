package com.resource.quickbite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class CanteenOrderAdapter(options: FirebaseRecyclerOptions<Order>) :
    FirebaseRecyclerAdapter<Order, CanteenOrderAdapter.OrderViewHolder>(options) {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderName: TextView = itemView.findViewById(R.id.tvOrderName)
        val tvFoodItem: TextView = itemView.findViewById(R.id.tvFoodItem)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvOrderTime: TextView = itemView.findViewById(R.id.tvOrderTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_canteen_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int, model: Order) {
        holder.tvOrderName.text = "Name: ${model.name}"
        holder.tvFoodItem.text = "Item: ${model.item}"
        holder.tvStatus.text = "Status: ${model.status}"
        holder.tvOrderTime.text = model.time
    }
}
