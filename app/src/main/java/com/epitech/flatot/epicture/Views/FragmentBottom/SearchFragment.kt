package com.epitech.flatot.epicture.Views.FragmentBottom

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.epitech.flatot.epicture.Adapter.SearchAdapter
import com.epitech.flatot.epicture.Interface.ILoadMore
import com.epitech.flatot.epicture.Model.ImgurModel
import com.epitech.flatot.epicture.Model.RetrofitModel
import com.epitech.flatot.epicture.R
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment(), Callback<ImgurModel.SearchResult>, ILoadMore {
    var items: MutableList<ImgurModel.ImgurSearchItem>? = null
    var new_items: MutableList<ImgurModel.ImgurSearchItem>? = null
    var searchQuery: String? = null
    lateinit var adapter: SearchAdapter
    var _page = 0

    override fun OnLoadMore() {
        if (new_items!!.size == items!!.size) {
            addAnotherPage()
        }
        val data = ImgurModel.Data_search("null", "null", "null", 0, "null", false, 0, 0, 0, 0, 0, 0, false, false, "null", "null", "null", false, false, false, emptyList(), emptyList(), "null")
        val item = ImgurModel.ImgurSearchItem(data)
        new_items!!.add(item)
        adapter.notifyItemInserted(new_items!!.size-1)
        Handler().postDelayed({
            new_items!!.removeAt(new_items!!.size-1)
            adapter.notifyItemRemoved(new_items!!.size)

            var count = new_items!!.size
            val need = count + 9

            while (count != items!!.size && count < need)
            {
                new_items!!.add(items!![count])
                count++
            }
            adapter.notifyDataSetChanged()
            adapter.setLoaded()
        }, 3000)
    }

    private fun addAnotherPage() {
        val imgurApi = RetrofitModel().createRetrofitBuilder()
        val token = arguments?.getString("access_token")
        val _window = "all"
        val _sort = "q_all"
        val _query = searchQuery!!
        _page++
        val call = imgurApi.searchGallery("Bearer " + token, _sort, _window, _page, _query)
        call.enqueue(object: Callback<ImgurModel.SearchResult> {
            override fun onFailure(call: Call<ImgurModel.SearchResult>, t: Throwable) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ImgurModel.SearchResult>, response: Response<ImgurModel.SearchResult>) {
                try {
                    if (response.isSuccessful) {
                        val picLists = response.body()
                        picLists!!.data.forEach { pic ->
                            val item = ImgurModel.ImgurSearchItem(pic)
                            items!!.add(item)
                        }
                    } else {
                        System.out.println(response.errorBody())

                    }
                }
                catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    companion object {
        fun newInstance(access_token: String): SearchFragment {
            val args = Bundle()
            args.putString("access_token", access_token)
            val fragment = SearchFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_search, container, false)
        try {
            if (items != null) {
                rootView.recyclerViewSearch.layoutManager = LinearLayoutManager(context)
                adapter = SearchAdapter(rootView.recyclerViewSearch, arguments?.getString("access_token")!!, context!!, items!!)
                rootView.recyclerViewSearch.adapter = adapter
            }
            val search = rootView.findViewById(R.id.searchView) as android.support.v7.widget.SearchView
            val searchEditText = search.findViewById<View>(android.support.v7.appcompat.R.id.search_src_text) as EditText
            search.isIconified = false
            searchEditText.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
            searchEditText.setHintTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
            rootView.searchView.queryHint = "Search Pictures in Imgur"
            rootView.searchView.setOnQueryTextListener(object : android.support.v7.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    searchQuery = query
                    items = ArrayList()
                    new_items = ArrayList()
                    //rootView.searchView.isIconified = true
                    //search.isIconified = true
                    rootView.progressBar.visibility = View.VISIBLE
                    rootView.searchView.clearFocus()
                    GetSearch()
                    return true
                }
            })
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
        return rootView
    }

    fun GetSearch() {
        val imgurApi = RetrofitModel().createRetrofitBuilder()
        val token = arguments?.getString("access_token")
        val _window = "all"
        val _sort = "q_all"
        val _query = searchQuery!!
        val call = imgurApi.searchGallery("Bearer " + token, _sort, _window, _page, _query)
        call.enqueue(this)
    }

    fun get_first_items(items: MutableList<ImgurModel.ImgurSearchItem>) {
        var count = 0

        while (count != items.size && count < 8)
        {
            new_items!!.add(items[count])
            count++
        }

    }

    override fun onFailure(call: Call<ImgurModel.SearchResult>, t: Throwable) {
        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onResponse(call: Call<ImgurModel.SearchResult>, response: Response<ImgurModel.SearchResult>) {
        try {
            if (response.isSuccessful) {
                val picLists = response.body()
                picLists!!.data.forEach { pic ->
                    val item = ImgurModel.ImgurSearchItem(pic)
                    items!!.add(item)
                }
                get_first_items(items!!)
                progressBar.visibility = View.GONE
                recyclerViewSearch.layoutManager = LinearLayoutManager(context)
                adapter = SearchAdapter(recyclerViewSearch, arguments?.getString("access_token")!!, context!!, new_items!!)
                recyclerViewSearch.adapter = adapter

                adapter.setLoadMore(this)
            } else {
                System.out.println(response.errorBody())

            }
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
    }
}
