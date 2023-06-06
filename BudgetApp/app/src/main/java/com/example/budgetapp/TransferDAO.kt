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

    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) AND (:date_from IS NULL OR date >= :date_from) AND (:date_to IS NULL OR date <= :date_to)")
    fun getFiltred(cat: String?, price_from: Double?, price_to: Double?, date_from: String?, date_to: String?): List<Transfer>

    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) AND (:date_from IS NULL OR date >= :date_from) AND (:date_to IS NULL OR date <= :date_to) ORDER BY label DESC")
    fun getFiltred_l_d(cat: String?, price_from: Double?, price_to: Double?, date_from: String?, date_to: String?): List<Transfer>
    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) AND (:date_from IS NULL OR date >= :date_from) AND (:date_to IS NULL OR date <= :date_to) ORDER BY label ASC")
    fun getFiltred_l_a(cat: String?, price_from: Double?, price_to: Double?, date_from: String?, date_to: String?): List<Transfer>
    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) AND (:date_from IS NULL OR date >= :date_from) AND (:date_to IS NULL OR date <= :date_to) ORDER BY amount DESC")
    fun getFiltred_p_d(cat: String?, price_from: Double?, price_to: Double?, date_from: String?, date_to: String?): List<Transfer>
    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) AND (:date_from IS NULL OR date >= :date_from) AND (:date_to IS NULL OR date <= :date_to) ORDER BY amount ASC")
    fun getFiltred_p_a(cat: String?, price_from: Double?, price_to: Double?, date_from: String?, date_to: String?): List<Transfer>
    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) AND (:date_from IS NULL OR date >= :date_from) AND (:date_to IS NULL OR date <= :date_to) ORDER BY date DESC")
    fun getFiltred_d_d(cat: String?, price_from: Double?, price_to: Double?, date_from: String?, date_to: String?): List<Transfer>
    @Query("SELECT * from transfers WHERE (:cat IS NULL OR category = :cat) AND (:price_from IS NULL OR amount > :price_from) AND (:price_to IS NULL OR amount < :price_to) AND (:date_from IS NULL OR date >= :date_from) AND (:date_to IS NULL OR date <= :date_to) ORDER BY date ASC")
    fun getFiltred_d_a(cat: String?, price_from: Double?, price_to: Double?, date_from: String?, date_to: String?): List<Transfer>

    @Insert
    fun insertAll(vararg transfer: Transfer)

    @Delete
    fun delete(transfer: Transfer)

    @Update
    fun update(vararg transfer: Transfer)
}