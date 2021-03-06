package com.marxist.android.ui.fragments.books

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.marxist.android.R
import com.marxist.android.database.entities.LocalBooks
import com.marxist.android.model.ShowSnackBar
import com.marxist.android.ui.base.BookClickListener
import com.marxist.android.utils.AppConstants
import com.marxist.android.utils.RxBus
import com.marxist.android.utils.api.ApiClient
import com.marxist.android.utils.download.DownloadUtil
import com.marxist.android.viewmodel.BookListViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragments_list.*
import kotlinx.android.synthetic.main.fragments_list.view.*
import kotlinx.android.synthetic.main.layout_lottie_no_feed.*
import okhttp3.ResponseBody
import okio.Okio
import retrofit2.Response
import java.io.File
import java.io.IOException
import kotlin.math.absoluteValue

class EBooksFragment : Fragment(), BookClickListener {
    override fun bookItemClickListener(adapterPosition: Int, book: LocalBooks) {
        if (book.isDownloaded) {
            DownloadUtil.openSavedBook(mContext, book)
        } else {
            if (book.downloadId == -1L) {
                val disposable =
                    download(
                        book,
                        AppConstants.BOOKS
                    ).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            bookListViewModel.updateStatus(it.absolutePath, true, book.bookid)
                            RxBus.publish(ShowSnackBar(getString(R.string.download_completed)))
                            bookAdapter.updateDownloadId(adapterPosition)
                        }, {
                            bookListViewModel.updateStatus("", false, book.bookid)
                            RxBus.publish(ShowSnackBar(getString(R.string.download_failed)))
                            bookAdapter.updateDownloadId(adapterPosition)
                        })
            }
        }
    }

    fun download(
        book: LocalBooks,
        type: String
    ): Observable<File> {
        return ApiClient.mDownloadService.downloadFileByUrlRx(book.epub)
            .flatMap(object : Function<Response<ResponseBody>, Observable<File>> {
                override fun apply(response: Response<ResponseBody>): Observable<File> {
                    try {
                        val extStorageDirectory = mContext.getExternalFilesDir(type)?.absolutePath
                        val filePath =
                            "${extStorageDirectory}/${book.title.hashCode().absoluteValue}.epub"
                        val file = File(filePath)
                        val sink = Okio.buffer(Okio.sink(file))
                        sink.writeAll(response.body()!!.source())
                        sink.close()

                        return Observable.just(file)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return Observable.error(e)
                    }
                }
            })
    }

    private lateinit var bookListViewModel: BookListViewModel
    private lateinit var bookAdapter: BookListAdapter
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        bookAdapter = BookListAdapter(mContext, mutableListOf(), this@EBooksFragment)
    }

    private fun initData() {
        bookListViewModel = ViewModelProviders.of(this).get(BookListViewModel::class.java)
        if (bookListViewModel.getLocalBooksSize() == 0) {
            rvListView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            lavEmptyImage.setAnimation(R.raw.loading_books)
        }
        bookListViewModel.callBookApi()
        bookListViewModel.getLocalBooks().observe(this, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    rvListView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    bookAdapter.setItems(it)
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragments_list, container, false)
        view.rvListView.setHasFixedSize(true)
        view.rvListView.layoutManager = GridLayoutManager(mContext, 2)
        view.rvListView.adapter = bookAdapter
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initData()
    }
}