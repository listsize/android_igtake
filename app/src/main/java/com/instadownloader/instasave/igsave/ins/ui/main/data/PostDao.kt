package com.instadownloader.instasave.igsave.ins.ui.main.data

import androidx.room.Dao
import androidx.room.*

@Dao
interface PostDao {
    //    @Query("SELECT * FROM " + MediaBean.TABLE_NAME +" ORDER BY time DESC")
    @Query("SELECT * FROM " + PostBean.TABLE_NAME )
    fun getAll(): List<PostBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: PostBean):Long

    @Update
    fun update(item: PostBean)

    @Delete
    fun delete(item: PostBean)

    @Query("DELETE FROM "+ PostBean.TABLE_NAME)
    fun deleteAll()
}