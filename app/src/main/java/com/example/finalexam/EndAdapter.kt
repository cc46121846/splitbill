import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalexam.EndItem
import com.example.finalexam.R

class EndAdapter(
    private val itemList: List<EndItem>,
    private val onSettled: (GroupViewHolder, EndItem) -> Unit
) :
    RecyclerView.Adapter<EndAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val youText: TextView = itemView.findViewById(R.id.youText)
        val othersText: TextView = itemView.findViewById(R.id.othersText)
        val sumText: TextView = itemView.findViewById(R.id.sumText)
        val settleButton: Button = itemView.findViewById(R.id.settleButton)
        val settleText: TextView = itemView.findViewById(R.id.settleText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_end, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = itemList[position]
        holder.youText.text = item.youText
        holder.othersText.text = item.othersText
        holder.sumText.text = item.sumText

        if (item.donePaid) {
            holder.settleButton.visibility = View.INVISIBLE;
            holder.settleText.visibility = View.VISIBLE;
        }

        holder.settleButton.setOnClickListener {
            onSettled(holder, item)
        }
    }

    override fun getItemCount() = itemList.size

    fun updateList(newList: List<EndItem>) {
        (itemList as MutableList).clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }
}
