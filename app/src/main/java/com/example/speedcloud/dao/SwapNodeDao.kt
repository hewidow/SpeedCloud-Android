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

    // 取前6条记录
    @Query("SELECT * FROM SwapNode WHERE type=:type AND task=0 ORDER BY time DESC LIMIT 6")
    fun getAllLoadedByType(type: Boolean): List<SwapNode>

    @Insert
    fun insertAll(vararg swapNode: SwapNode)

    @Query("DELETE FROM SwapNode")
    fun deleteAll()

    @Query("DELETE FROM SwapNode WHERE id=:id")
    fun deleteById(id: Int)

    @Update
    fun updateAll(vararg swapNode: SwapNode)

    @Query("UPDATE SwapNode SET task=0 WHERE task=:task")
    fun updateByTask(task: Long)
}