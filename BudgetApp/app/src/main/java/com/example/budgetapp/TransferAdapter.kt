package com.example.budgetapp

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class TransferAdapter(private var transfers: List<Transfer>) :
    RecyclerView.Adapter<TransferAdapter.TransferHolder>() {

    class TransferHolder(view: View) : RecyclerView.ViewHolder(view){
        val title : TextView = view.findViewById(R.id.title)
        val amount : TextView = view.findViewById(R.id.amount)
        val cat : TextView = view.findViewById(R.id.cat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transfer_layout, parent, false)
        return TransferHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransferHolder, position: Int) {
        val transfer = transfers[position]
        val context = holder.amount.context
        if(transfer.amount >= 0){
            holder.amount.text = "+ %.2f".format(transfer.amount) + " pln"
            holder.amount.setTextColor(ContextCompat.getColor(context,R.color.green))
        }
        else{
            holder.amount.text = "- %.2f".format(abs(transfer.amount)) + " pln"
            holder.amount.setTextColor(ContextCompat.getColor(context,R.color.red))
        }
        holder.title.text = transfer.label
        holder.cat.text = transfer.category

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra("transfer", transfer)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return transfers.size
    }

    fun setData(transfers: List<Transfer>){
        this.transfers = transfers
        notifyDataSetChanged()
    }
}