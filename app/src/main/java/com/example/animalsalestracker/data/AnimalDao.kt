package com.example.animalsalestracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.animalsalestracker.data.Animal


@Dao
interface AnimalDao {
    @Query("SELECT * FROM Animals")
    fun getAll(): Flow<List<Animal>>

    @Query("SELECT a.* " +
            "FROM  Animals AS a " +
            "LEFT JOIN orders AS o " +
            "ON o.animalId =  a.animalId " +
            "WHERE o.animalId IS NULL")
    fun  getAnimalsWithoutOrders(): Flow<List<Animal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(animal: Animal): Long

    @Update suspend fun update(animal: Animal)
    @Delete suspend fun delete(animal: Animal)

    @Query("DELETE FROM Animals WHERE animalId = 1")
    suspend fun deleteCompleted()

    @Query("DELETE FROM Animals")
    suspend fun deleteAll()
}