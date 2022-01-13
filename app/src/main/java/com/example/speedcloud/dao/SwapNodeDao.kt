package com.example.speedcloud.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.speedcloud.bean.SwapNode

@Dao
interface SwapNodeDao {
    @Query("SELECT * FROM SwapNode WHERE type=:type AND task!=0")
    fun getAllLoadingByType(type: Boolean): List<SwapNode>

    @Query("SELECT * FROM SwapNode WHERE type=:type AND task=0 ORDER BY time DESC LIMIT :limit OFFSET :offset")
    fun getAllLoadedByType(type: Boolean, limit: Int, offset: Int): List<SwapNode>

    @Insert
    fun insertAll(vararg swapNode: SwapNode)

    @Query("DELETE FROM SwapNode")
    fun deleteAll()

    @Query("DELETE FROM SwapNode WHERE task=:task")
    fun deleteByTask(task: Long)

    @Update
    fun updateAll(vararg swapNode: SwapNode)

    @Query("UPDATE SwapNode SET task=0 WHERE task=:task")
    fun updateByTask(task: Long)
}