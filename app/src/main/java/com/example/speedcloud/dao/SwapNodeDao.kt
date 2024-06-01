package com.example.speedcloud.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.speedcloud.bean.SwapNode

@Dao
interface SwapNodeDao {

    @Query("SELECT * FROM SwapNode WHERE type=:type ORDER BY time DESC LIMIT :limit OFFSET :offset")
    fun getAllLoadedByType(type: Boolean, limit: Int, offset: Int): List<SwapNode>

    @Insert
    fun insertAll(vararg swapNode: SwapNode)

    @Query("DELETE FROM SwapNode")
    fun deleteAll()
    
    @Update
    fun updateAll(vararg swapNode: SwapNode)

}