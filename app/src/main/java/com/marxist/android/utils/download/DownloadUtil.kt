package com.marxist.android.utils.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.marxist.android.R
import com.marxist.android.database.AppDatabase
import com.marxist.android.database.entities.LocalBooks
import com.marxist.android.utils.AppConstants
import org.geometerplus.android.fbreader.FBReader
import kotlin.math.absoluteValue

object DownloadUtil {
    fun queueForDownload(
        mContext: Context,
        title: String,
        url: String,
        pubDate: Long,
        type: String, bookId: String = ""
    ): Long {
        val downloadManager =
            mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(title)
        request.setDescription(mContext.getString(R.string.download_started))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE or DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        var filePath = ""

        when (type) {
            AppConstants.AUDIO -> {
                val extStorageDirectory = mContext.getExternalFilesDir(type)?.absolutePath
                filePath =
                    "${extStorageDirectory}/${title.hashCode().absoluteValue}.mp3"
            }
        }

        request.setDestinationUri(Uri.parse("file://$filePath"))
        val id = downloadManager.enqueue(request)
        when (type) {
            AppConstants.AUDIO -> {
                PreferencesHelper.saveAudioDownload(mContext, id)
                val localFeedsDao = AppDatabase.getAppDatabase(mContext).localFeedsDao()
                localFeedsDao.updateAudioStatus(filePath, title, pubDate)
            }
        }
        return id
    }

    fun openSavedBook(context: Context, book: LocalBooks) {
        FBReader.openBookActivity(context, book.savedPath)
    }
}