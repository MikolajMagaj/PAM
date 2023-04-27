package com.example.budgetapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var deleteTransfer: Transfer
    private lateinit var oldTransfers : List<Transfer>
    private lateinit var transfers : List<Transfer>
    private lateinit var transferAdapter : TransferAdapter
    private lateinit var linearLayoutManager : LinearLayoutManager
    private lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transfers = arrayListOf()
        transferAdapter = TransferAdapter(transfers)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "transfers").fallbackToDestructiveMigration().build()

        recyclerview.apply {
            adapter = transferAdapter
            layoutManager = linearLayoutManager

            val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    deleteTransfer(transfers[viewHolder.adapterPosition])
                }

            }

            val swipeHelper = ItemTouchHelper(itemTouchHelper)
            swipeHelper.attachToRecyclerView(recyclerview)

            addButton.setOnClickListener{
                val intent = Intent(this@MainActivity, AddTransferActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun fetchAll(){
        GlobalScope.launch {
            transfers = db.transferDAO().getAll()

            runOnUiThread{
                transfersUpdate()
                transferAdapter.setData(transfers)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun transfersUpdate(){
        val totalAmount = transfers.map { it.amount }.sum()
        val budgetAmount = transfers.filter { it.amount > 0 }.map { it.amount }.sum()
        val expenseAmount = totalAmount - budgetAmount

        balance.text = "%.2f".format(totalAmount) + " pln"
        income.text = "%.2f".format(budgetAmount) + " pln"
        expense.text = "%.2f".format(expenseAmount) + " pln"
    }

    private fun undoDelete(){
        GlobalScope.launch {
            db.transferDAO().insertAll(deleteTransfer)
            transfers = oldTransfers
            runOnUiThread {
                transferAdapter.setData(transfers)
                transfersUpdate()
            }
        }
    }

    private fun showError(){
        val view = findViewById<View>(R.id.coordinator)
        val errorMsg = Snackbar.make(view, "Usunięto transakcję!", Snackbar.LENGTH_LONG)
        errorMsg.setAction("Cofnij"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransfer(transfer: Transfer){
        deleteTransfer = transfer
        oldTransfers = transfers

        GlobalScope.launch {
            db.transferDAO().delete(transfer)

            transfers = transfers.filter { it.id != transfer.id }
            runOnUiThread {
                transfersUpdate()
                transferAdapter.setData(transfers)
                showError()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}