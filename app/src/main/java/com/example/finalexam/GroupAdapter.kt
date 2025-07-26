import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalexam.EditAmount
import com.example.finalexam.Group
import com.example.finalexam.GroupItem
import com.example.finalexam.R

class GroupAdapter(
    var itemList: List<GroupItem>,
    private val onDelete: (GroupItem) -> Unit
) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupText_item: TextView = itemView.findViewById(R.id.groupText_item)
        val youneedpayText: TextView = itemView.findViewById(R.id.youneedpayText)
        val youcangetText: TextView = itemView.findViewById(R.id.youcangetText)
        val TypeText: TextView = itemView.findViewById(R.id.sumText)
        val deleteButton_group: ImageButton = itemView.findViewById(R.id.deleteButton_group)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = itemList[position]
        val context = holder.itemView.context
        holder.groupText_item.text = item.groupText_item
        holder.youcangetText.visibility = View.INVISIBLE
        holder.youneedpayText.visibility = View.INVISIBLE

        if (item.youneedpayText.toFloat() > 0) {
            holder.youneedpayText.visibility = View.VISIBLE
            holder.youneedpayText.text = "我一共欠了：${item.youneedpayText}"
        } else {
            val money = item.youneedpayText.toFloat() * -1
            holder.youcangetText.visibility = View.VISIBLE
            holder.youcangetText.text = "我一共可得：${money}"
        }
        holder.TypeText.text = item.TypeText

        holder.itemView.setOnClickListener {
            val intent = Intent(context, Group::class.java)
            intent.putExtra("GroupName", item.groupText_item)
            intent.putExtra("group_id", item.group_id)
            context.startActivity(intent)
        }
        holder.deleteButton_group.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount() = itemList.size

    fun updateList(newList: List<GroupItem>) {
        itemList = newList
        notifyDataSetChanged()
    }
}
