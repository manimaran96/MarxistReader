package com.marxist.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.marxist.android.database.entities.LocalBooks

@Dao
interface LocalBooksDao {
    @Query("SELECT * from localBooks")
    fun getAllLocalBooks(): LiveData<MutableList<LocalBooks>>

    @Query("SELECT * from localBooks")
    fun getAllBooks(): MutableList<LocalBooks>

    @Insert(onConflict = REPLACE)
    fun insert(localBooks: LocalBooks)

    @Delete
    fun deleteBook(localBooks: LocalBooks)

    @Query("SELECT EXISTS(SELECT bookid from localBooks WHERE bookid = :bookId)")
    fun isIdAvailable(bookId: String): Boolean

    @Query("UPDATE localBooks SET saved_path = :filePath, is_downloaded = :isDownloaded WHERE bookid = :bookId")
    fun updateDownloadDetails(filePath: String, isDownloaded: Boolean, bookId: String)

    @Query("UPDATE localBooks SET download_id = :downloadId WHERE bookid = :bookId")
    fun updateDownloadId(downloadId: Long, bookId: String)

    @Query("UPDATE localBooks SET is_downloaded = :isDownloaded WHERE download_id = :downloadId")
    fun updateBookStatus(isDownloaded: Boolean, downloadId: Long)
}