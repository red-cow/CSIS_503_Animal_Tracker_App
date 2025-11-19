package com.example.animalsalestracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val customerId: Long = 0L,
    val name: String,
    val email: String = "",
    val phone: String = ""
)