package com.example.animalsalestracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["animalId"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE // deleting an Animal deletes its Orders
        ),
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["customerId"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT // prevents deleting a Customer with Orders
        )
    ],
    indices = [
        Index(value = ["animalId"]),
        Index(value = ["customerId"])
    ]
)
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // Foreign keys
    val animalId: Long?,
    val customerId: Long?,

    // Money fields: store in cents to avoid floating-point errors
    val deposit: Double = 0.0,
    val payment: Double = 0.0,

    // Ready date (nullable): epoch millis; null if not scheduled yet
    val readyDate: String,

    // Purchase completed?
    val purchaseComplete: Boolean = false
)