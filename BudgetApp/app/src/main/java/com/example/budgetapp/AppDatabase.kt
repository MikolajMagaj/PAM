package com.example.budgetapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transfer::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun transferDAO() : TransferDAO
}