package com.example.budgetapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransferDAO {
    @Query("SELECT * from transfers")
    fun getAll(): List<Transfer>

    @Insert
    fun insertAll(vararg transfer: Transfer)

    @Delete
    fun delete(transfer: Transfer)

    @Update
    fun update(vararg transfer: Transfer)
}