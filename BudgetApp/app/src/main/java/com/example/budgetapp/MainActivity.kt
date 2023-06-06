package com.example.budgetapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.*

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
    var selectedDateFrom: String? = null
    var selectedDateTo: String? = null



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
                    fetchFiltred(selectedOption_cat, price_from, price_to, selectedDateFrom, selectedDateTo)
                }
                else {
                    fetchAll()
                    selectedOption_cat = null
                }
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
                    fetchFiltred2(selectedOption_cat, price_from, price_to, selectedDateFrom, selectedDateTo, position)
                }
                else
                    fetchFiltred(selectedOption_cat, price_from, price_to, selectedDateFrom, selectedDateTo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Obsługa braku wybranego elementu
            }
        }

        etPriceFrom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                price_from = etPriceFrom.text.toString().toDoubleOrNull()
                fetchFiltred(selectedOption_cat, price_from, price_to, selectedDateFrom, selectedDateTo)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        etPriceTo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                price_to = etPriceTo.text.toString().toDoubleOrNull()
                fetchFiltred(selectedOption_cat, price_from, price_to, selectedDateFrom, selectedDateTo)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        etDateFrom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                fetchFiltred(selectedOption_cat, price_from, price_to, selectedDateFrom, selectedDateTo)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        etDateTo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                fetchFiltred(selectedOption_cat, price_from, price_to, selectedDateFrom, selectedDateTo)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

    }

    private fun fetchFiltred(selectedOption: String?, price_f: Double?, price_t: Double?, date_f: String?, date_t: String?) {
        GlobalScope.launch {
            transfers = db.transferDAO().getFiltred(selectedOption, price_f, price_t, date_f, date_t)

            runOnUiThread{
                transferAdapter.setData(transfers)
            }
        }
    }
    private fun fetchFiltred2(selectedOption: String?, price_f: Double?, price_t: Double?, date_f: String?, date_t: String?, pos: Int) {
        GlobalScope.launch {
            if(pos == 1)
                transfers = db.transferDAO().getFiltred_p_d(selectedOption, price_f, price_t, date_f, date_t)
            else if(pos == 2)
                transfers = db.transferDAO().getFiltred_p_a(selectedOption, price_f, price_t, date_f, date_t)
            else if(pos == 3)
                transfers = db.transferDAO().getFiltred_l_d(selectedOption, price_f, price_t, date_f, date_t)
            else if(pos == 4)
                transfers = db.transferDAO().getFiltred_l_a(selectedOption, price_f, price_t, date_f, date_t)
            else if(pos == 5)
                transfers = db.transferDAO().getFiltred_d_d(selectedOption, price_f, price_t, date_f, date_t)
            else if(pos == 6)
                transfers = db.transferDAO().getFiltred_d_a(selectedOption, price_f, price_t, date_f, date_t)
            else
                transfers = db.transferDAO().getFiltred(selectedOption, price_f, price_t, date_f, date_t)
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

    fun showDatePickerDialogFrom(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selDate = Calendar.getInstance()
            selDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(selDate.time)
            selectedDateFrom = formattedDate
            etDateFrom.setText(formattedDate)
        }, year, month, day)

        datePickerDialog.setOnCancelListener {
            selectedDateFrom = null
            etDateFrom.setText(selectedDateFrom)
        }

        datePickerDialog.show()
    }
    fun showDatePickerDialogTo(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selDate = Calendar.getInstance()
            selDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(selDate.time)
            selectedDateTo = formattedDate
            etDateTo.setText(formattedDate)
        }, year, month, day)

        datePickerDialog.setOnCancelListener {
            selectedDateTo = null
            etDateTo.setText(selectedDateTo)
        }

        datePickerDialog.show()
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}