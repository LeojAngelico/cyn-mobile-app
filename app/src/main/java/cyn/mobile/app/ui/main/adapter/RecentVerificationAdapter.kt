package cyn.mobile.app.ui.main.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cyn.mobile.app.data.model.RecentVerificationItem
import cyn.mobile.app.databinding.ItemRecentVerificationBinding

class RecentVerificationAdapter : RecyclerView.Adapter<RecentVerificationAdapter.VH>() {

    // Internal mutable list for full control
    private val items = mutableListOf<RecentVerificationItem>()

    class VH(val binding: ItemRecentVerificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRecentVerificationBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.titleTextView.text = item.title
        holder.binding.phoneTextView.text = item.phoneNumber
        holder.binding.statusTextView.text = item.status

        // Simple color cue
        holder.binding.statusTextView.setTextColor(
            when(item.status.lowercase()){
                "success", "verified", "received", "generated" -> Color.parseColor("#2E7D32")
                else -> Color.parseColor("#C62828")
            }
        )
    }

    override fun getItemCount() = items.size

    // Clear all items
    fun clearItems() {
        val size = items.size
        if (size == 0) return
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    // Add a single item
    fun addItem(item: RecentVerificationItem) {
        val insertPos = items.size
        items.add(item)
        notifyItemInserted(insertPos)
    }

    // Add a list of items
    fun addItems(newItems: List<RecentVerificationItem>) {
        if (newItems.isEmpty()) return
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    // Replace the entire data set
    fun replaceAll(newItems: List<RecentVerificationItem>) {
        val oldSize = items.size
        items.clear()
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize)
        if (newItems.isNotEmpty()) {
            items.addAll(newItems)
            notifyItemRangeInserted(0, newItems.size)
        }
    }
}
