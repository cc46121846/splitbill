package com.example.finalexam

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    var itemList: List<ItemItem>,
    private val onDelete: (ItemItem) -> Unit,
    private val onEdit: (ItemItem) -> Unit) :
    RecyclerView.Adapter<ItemAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.youText)
        val billNameText: TextView = itemView.findViewById(R.id.billnameText)
        val whoPaidText: TextView = itemView.findViewById(R.id.whopayCheckText)
        val youOweText: TextView = itemView.findViewById(R.id.youbowText)
        val youPaidText: TextView = itemView.findViewById(R.id.youpayText)
        val remarkText: TextView = itemView.findViewById(R.id.remarkCheckText)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton_item)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_item, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = itemList[position]
        holder.dateText.text = item.date
        holder.billNameText.text = item.billName
        holder.whoPaidText.text = "${item.whoPaid} 付了 ${item.amount}"
        if (item.youPaid == "0") {
            holder.youPaidText.visibility = View.GONE
        } else {
            holder.youPaidText.visibility = View.VISIBLE
            holder.youPaidText.text = "我付了：${item.youPaid}"
        }
        if (item.youOwe == "0") {
            holder.youOweText.visibility = View.GONE
        } else {
            holder.youOweText.visibility = View.VISIBLE
            holder.youOweText.text = "我欠款：${item.youOwe}"
        }
        holder.remarkText.text = "備註：${item.remark}"

        holder.editButton.setOnClickListener {
          onEdit(item)
        }
        holder.deleteButton.setOnClickListener {
            onDelete(item)
        }

    }

    override fun getItemCount() = itemList.size
    fun updateList(itemItems: List<ItemItem>) {
        itemList = itemItems
        notifyDataSetChanged()
    }
}
