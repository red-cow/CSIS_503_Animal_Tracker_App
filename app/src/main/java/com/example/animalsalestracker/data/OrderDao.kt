package com.example.animalsalestracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders")
    fun getAll(): Flow<List<Order>>

    @Query("SELECT * FROM Animals WHERE animalId = :animalId")
    fun getAnimalById(animalId: Long?): Animal

    @Query("SELECT * FROM Customers WHERE customerId = :customerId")
    fun getCustomerById(customerId: Long?): Customer

    @Query("SELECT COUNT(*) FROM orders WHERE purchaseComplete = 0")
    fun totalOpenOrders(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE purchaseComplete = 1")
    fun totalCloseOrders(): Flow<Int>

    @Query("SELECT SUM(deposit) FROM orders")
    fun totalDownpayment(): Flow<Double>

    @Query("SELECT SUM(payment) FROM orders")
    fun totalPayment(): Flow<Double>

    @Query("SELECT COUNT(*) " +
            "FROM Animals AS a " +
            "LEFT JOIN orders AS o " +
            "ON o.animalId = a.animalId " +
            "WHERE o.purchaseComplete = 1 ")
    fun animalsSold(): Flow<Int>

    @Query("SELECT COUNT(*) " +
            "FROM Animals AS a " +
            "LEFT JOIN orders AS o " +
            "ON o.animalId = a.animalId " +
            "WHERE o.animalId IS NULL ")
    fun animalsAvailable(): Flow<Int>


    @Update suspend fun update(order: Order)
    @Delete suspend fun delete(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(order: Order): Long
//    @Query(
//        """
//        SELECT
//            -- 1. open orders
//            (SELECT COUNT(*) FROM orders WHERE purchaseComplete = 0) AS totalOpenOrders,
//
//            -- 2. closed orders
//            (SELECT COUNT(*) FROM orders WHERE purchaseComplete = 1) AS totalClosedOrders,
//
//            -- 3. total downpayment
//            (SELECT SUM(deposit) FROM orders) AS totalDownPayment,
//
//            -- 4. total payment
//            (SELECT SUM(payment) FROM orders) AS totalPayment,
//
//            -- 5. animals sold (animals that have an order marked complete)
//            (SELECT COUNT(*)
//               FROM Animals AS a
//               LEFT JOIN orders AS o ON o.animalId = a.animalId
//               WHERE o.purchaseComplete = 1
//            ) AS animalsSold,
//
//            -- 6. animals available (animals with NO order)
//            (SELECT COUNT(*)
//               FROM Animals AS a
//               LEFT JOIN orders AS o ON o.animalId = a.animalId
//               WHERE o.animalId IS NULL
//            ) AS animalsAvailable
//        """
//    )
//    suspend fun getDashboardStats(): DashboardStats


}