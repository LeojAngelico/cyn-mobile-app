package cyn.mobile.app.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cyn.mobile.app.R
import cyn.mobile.app.data.repositories.transaction.response.TransactionListItem

class TransactionHistoryAdapter(
    private val items: List<TransactionListItem>,
    private val onCopyTransactionId: (String) -> Unit = {}
) : RecyclerView.Adapter<TransactionHistoryAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTxnId: TextView = view.findViewById(R.id.tvTxnId)
        val tvSessionId: TextView = view.findViewById(R.id.tvSessionId)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val chipAuthCode: TextView = view.findViewById(R.id.chipAuthCode)
        val chipGenerateToken: TextView = view.findViewById(R.id.chipGenerateToken)
        val chipNumberVerification: TextView = view.findViewById(R.id.chipNumberVerification)
        val chipStatus: TextView = view.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvTxnId.text = item.transaction_id.orEmpty()
        holder.tvSessionId.text = item.session_id.orEmpty()
        holder.tvDateTime.text = item.transaction_date?.datetime_ph.orEmpty()
        holder.tvPhone.text = item.phone_number.orEmpty()

        setChip(holder.chipAuthCode, item.auth_code.orEmpty())
        setChip(holder.chipGenerateToken, item.token_status.orEmpty())
        setChip(holder.chipNumberVerification, item.number_verification.orEmpty())
        setChip(holder.chipStatus, item.final_status.orEmpty())

        holder.tvTxnId.setOnClickListener {
            onCopyTransactionId(item.transaction_id.orEmpty())
        }
    }

    override fun getItemCount(): Int = items.size

    private fun setChip(tv: TextView, status: String) {
        tv.text = status
        val ctx = tv.context
        val (bg, color) = when (status.lowercase()) {
            "success", "verified", "generated", "received" -> R.drawable.bg_chip_success to R.color.on_success_chip
            "failed" -> R.drawable.bg_chip_failed to R.color.on_failed_chip
            "pending" -> R.drawable.bg_chip_pending to R.color.on_pending_chip
            else -> R.drawable.bg_chip_pending to R.color.on_pending_chip
        }
        tv.background = ContextCompat.getDrawable(ctx, bg)
        tv.setTextColor(ContextCompat.getColor(ctx, color))
    }
}
