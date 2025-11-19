package com.example.animalsalestracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Animals")
data class Animal(
    @PrimaryKey(autoGenerate = true) val animalId: Long = 0L,
    val name: String,
    val type: String = "",
    val dob: String = "",
    val weight: Double = 0.0,
    val buyerId: Long? = null,
)