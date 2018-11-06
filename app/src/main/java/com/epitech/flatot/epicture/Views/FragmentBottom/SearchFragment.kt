package com.epitech.flatot.epicture.Views.FragmentBottom

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import com.epitech.flatot.epicture.Adapter.SearchAdapter
import com.epitech.flatot.epicture.Model.ImgurInterface
import com.epitech.flatot.epicture.Model.RetrofitInterface
import com.epitech.flatot.epicture.R
import com.epitech.flatot.epicture.Views.BottomNavActivity
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.EditText
import android.widget.ImageView
import com.epitech.flatot.epicture.R.id.searchView






class SearchFragment : Fragment(), Callback<ImgurInterface.SearchResult> {

    var items: MutableList<ImgurInterface.ImgurSearchItem>? = null
    var searchQuery: String? = null

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
        if (items != null)
        {
            rootView.recyclerViewSearch.layoutManager = LinearLayoutManager(context)
            val adapter = SearchAdapter(arguments?.getString("access_token")!!, context!!, items!!)
            rootView.recyclerViewSearch.adapter = adapter
        }
        val search = rootView.findViewById(R.id.searchView) as android.support.v7.widget.SearchView
        val searchEditText = search.findViewById<View>(android.support.v7.appcompat.R.id.search_src_text) as EditText
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
                searchView.isIconified = true
                GetSearch()
                return true
            }
        })
        return rootView
    }

    fun GetSearch() {
        val imgurApi = RetrofitInterface().createRetrofitBuilder()
        val token = arguments?.getString("access_token")
        val _window = "all"
        val _sort = "q_all"
        val _page = 1
        val _query = searchQuery!!
        val call = imgurApi.searchGallery("Bearer " + token, _sort, _window, _page, _query)
        call.enqueue(this)
    }

    override fun onFailure(call: Call<ImgurInterface.SearchResult>, t: Throwable) {
        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onResponse(call: Call<ImgurInterface.SearchResult>, response: Response<ImgurInterface.SearchResult>) {
        if (response.isSuccessful) {
            val picLists = response.body()
            picLists!!.data.forEach {
                pic ->
                val item = ImgurInterface.ImgurSearchItem(pic)
                items!!.add(item)
            }
            recyclerViewSearch.layoutManager = LinearLayoutManager(context)
            val adapter = SearchAdapter(arguments?.getString("access_token")!!, context!!, items!!)
            recyclerViewSearch.adapter = adapter
        }
        else {
            System.out.println(response.errorBody())
        }
    }
}
