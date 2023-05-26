package com.example.budgetapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_transfer.*
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details.titleInput
import kotlinx.android.synthetic.main.activity_details.titleLayout
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

    private lateinit var spinner: Spinner
    private lateinit var spinner1: Spinner
    var selectedOption_cat: String? = null
    var price_from: Double? = null
    var price_to: Double? = null
    var order_name: String? = null
    var order: String? = null


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

        spinner = findViewById(R.id.category_pick)

        val adapter = ArrayAdapter.createFromResource(this, R.array.spinner_items, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position > 0) {
                    selectedOption_cat = parent?.getItemAtPosition(position).toString()
                    fetchFiltred(selectedOption_cat, price_from, price_to, order_name, order)
                }
                else
                    fetchAll()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Obsługa braku wybranego elementu
            }
        }
        spinner1 = findViewById(R.id.sort_pick)

        val adapter1 = ArrayAdapter.createFromResource(this, R.array.spinner_sort_items, android.R.layout.simple_spinner_item)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1
        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position > 0) {
                    if(position == 1)
                    {
                        order_name = "amount"
                        order = "DESC"
                    }

                    if(position == 2)
                    {
                        order_name = "amount"
                        order = "ASC"
                    }
                    if(position == 3)
                    {
                        order_name = "label"
                        order = "DESC"
                    }
                    if(position == 4)
                    {
                        order_name = "label"
                        order = "ASC"
                    }
                    if(position == 5)
                    {
                        order_name = "date"
                        order = "ASC"
                    }
                    if(position == 6)
                    {
                        order_name = "date"
                        order = "ASC"
                    }
                    fetchFiltred(selectedOption_cat, price_from, price_to, order_name, order)
                }
                else
                    fetchAll()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Obsługa braku wybranego elementu
            }
        }

        etPriceFrom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                price_from = etPriceFrom.text.toString().toDoubleOrNull()
                fetchFiltred(selectedOption_cat, price_from, price_to, order_name, order)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        etPriceTo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                price_to = etPriceTo.text.toString().toDoubleOrNull()
                fetchFiltred(selectedOption_cat, price_from, price_to, order_name, order)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


    }

    private fun fetchFiltred(selectedOption: String?, price_f: Double?, price_t: Double?, order_name: String?, order: String?) {
        GlobalScope.launch {
            transfers = db.transferDAO().getFiltred(selectedOption, price_f, price_t, order_name, order)

            runOnUiThread{
                transferAdapter.setData(transfers)
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