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

    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) ORDER BY CASE WHEN :order_name IS NOT NULL AND :order IS NOT NULL THEN :order_name || ' ' || :order ELSE '' END")
    fun getFiltred(cat: String?, price_from: Double?, price_to: Double?, order_name: String?, order: String?): List<Transfer>

    @Insert
    fun insertAll(vararg transfer: Transfer)

    @Delete
    fun delete(transfer: Transfer)

    @Update
    fun update(vararg transfer: Transfer)
}