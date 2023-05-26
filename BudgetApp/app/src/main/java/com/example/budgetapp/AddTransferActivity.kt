package com.example.budgetapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.budgetapp.databinding.ActivityAddTransferBinding
import kotlinx.android.synthetic.main.activity_add_transfer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AddTransferActivity : AppCompatActivity(){

    private val IMAGE_CAPTURE_CODE: Int = 1001
    private val PERMISSION_CODE: Int = 1000
    private lateinit var spinner: Spinner
    lateinit var selectedOption: String
    lateinit var selectedDate: String
    var image_uri: Uri? = null

    private lateinit var binding: ActivityAddTransferBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)


        spinner = findViewById(R.id.category)
        val adapter = ArrayAdapter.createFromResource(this, R.array.spinner_items, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOption = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Obsługa braku wybranego elementu
            }
        }


        titleInput.addTextChangedListener {
            if(it!!.isNotEmpty())
                titleLayout.error = null
        }
        amountInput.addTextChangedListener {
            if(it!!.isNotEmpty())
                amountLayout.error = null
        }

        binding.imageView.setOnClickListener{
            if(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                openCamera()
            }
        }


        addTransButton.setOnClickListener{
            val title = titleInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val description = descInput.text.toString()
            val url = image_uri.toString()

            if(title.isEmpty())
                titleLayout.error = "Wprowadź poprawną nazwę"

            else if(amount == null)
                amountLayout.error = "Wprowadź poprawną kwotę"
            else {
                val transfer = Transfer(0, title, amount, url, description, selectedOption, selectedDate)
                insert(transfer)
            }
        }

        closeButton.setOnClickListener{
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera()
                } else {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            imageView.setImageURI(image_uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun insert(transfer: Transfer){
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transfers")
            .build()
        GlobalScope.launch {
            db.transferDAO().insertAll(transfer)
            finish()
        }
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