package com.marxist.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.marxist.android.database.entities.LocalFeeds

@Dao
interface LocalFeedsDao {
    @Query("SELECT * FROM localFeeds ORDER BY pub_date DESC")
    fun getAllFeeds(): LiveData<MutableList<LocalFeeds>>

    @Query("SELECT count(*) FROM localFeeds")
    fun getFeedsCount(): Int

    @Insert(onConflict = IGNORE)
    fun insert(localBooks: LocalFeeds)

    @Query("UPDATE localFeeds SET is_bookmarked = :isBookMarked WHERE title = :title AND pub_date = :pubDate")
    fun updateBookMarkStatus(isBookMarked: Boolean, title: String, pubDate: Long)

    @Query("UPDATE localFeeds SET read_percent = :readPercent WHERE title = :title AND pub_date = :pubDate")
    fun updateReadPercentage(readPercent: Int, title: String, pubDate: Long)
}