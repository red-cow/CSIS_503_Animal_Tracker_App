package com.example.animalsalestracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.animalsalestracker.data.Customer
@Dao
interface CustomerDao {
    @Query("SELECT * FROM Customers")
    fun getAll(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: Customer): Long

    @Update suspend fun update(customer: Customer)
    @Delete suspend fun delete(customer: Customer)

    @Query("DELETE FROM Customers WHERE customerId = 1")
    suspend fun deleteCompleted()
}