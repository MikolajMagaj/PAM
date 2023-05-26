package com.example.budgetapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_add_transfer.*
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details.amountInput
import kotlinx.android.synthetic.main.activity_details.amountLayout
import kotlinx.android.synthetic.main.activity_details.closeButton
import kotlinx.android.synthetic.main.activity_details.dateEditText
import kotlinx.android.synthetic.main.activity_details.descInput
import kotlinx.android.synthetic.main.activity_details.titleInput
import kotlinx.android.synthetic.main.activity_details.titleLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class DetailsActivity : AppCompatActivity() {

    private val IMAGE_CAPTURE_CODE: Int = 1001
    private lateinit var transfer: Transfer
    private lateinit var spinner: Spinner
    lateinit var selectedOption: String
    lateinit var selectedDate: String
    var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        transfer = intent.getSerializableExtra("transfer") as Transfer

        titleInput.setText(transfer.label)
        amountInput.setText(transfer.amount.toString())
        descInput.setText(transfer.description)
        photoView.setImageURI(transfer.img_uri?.toUri())

        spinner = findViewById(R.id.category)

        val adapter = ArrayAdapter.createFromResource(this, R.array.spinner_items, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(adapter.getPosition(transfer.category))

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOption = parent?.getItemAtPosition(position).toString()
                if(selectedOption != transfer.category)
                    updateTransButton.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Obsługa braku wybranego elementu
            }
        }


        rootView.setOnClickListener{
            this.window.decorView.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        titleInput.addTextChangedListener {
            updateTransButton.visibility = View.VISIBLE
            if(it!!.isNotEmpty())
                titleLayout.error = null
        }
        amountInput.addTextChangedListener {
            updateTransButton.visibility = View.VISIBLE
            if(it!!.isNotEmpty())
                amountLayout.error = null
        }
        descInput.addTextChangedListener {
            updateTransButton.visibility = View.VISIBLE
        }

        addPhoto.setOnClickListener {
            openCamera()
            updateTransButton.visibility = View.VISIBLE
            photoView.setImageURI(image_uri)
        }

        updateTransButton.setOnClickListener{
            val title = titleInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val description = descInput.text.toString()
            val url = image_uri.toString()

            if(title.isEmpty())
                titleLayout.error = "Wprowadź poprawną nazwę"

            else if(amount == null)
                amountLayout.error = "Wprowadź poprawną kwotę"
            else {
                transfer = Transfer(transfer.id, title, amount, url, description, selectedOption, selectedDate)
                update(transfer)
            }
        }

        closeButton.setOnClickListener{
            finish()
        }
    }
    private fun update(transfer: Transfer){
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transfers")
            .build()
        GlobalScope.launch {
            db.transferDAO().update(transfer)
            finish()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            photoView.setImageURI(image_uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showDatePickerDialog(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
            dateEditText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }
}