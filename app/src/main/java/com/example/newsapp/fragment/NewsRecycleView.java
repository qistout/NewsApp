package com.example.newsapp.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ajguan.library.EasyRefreshLayout;
import com.example.newsapp.NewsDetailActivity;
import com.example.newsapp.R;
import com.example.newsapp.adapter.MyNewsListAdapter;
import com.example.newsapp.model.NewsData;
import com.example.newsapp.model.SingleNews;
import com.example.newsapp.network.GetDataService;
import com.example.newsapp.network.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsRecycleView extends Fragment {
    private View view;
    // search options
    private String size = "20";
    private String startDate = "2019-07-01 13:12:45";
    private String endDate   = "2021-08-03 18:42:20";
    private String words = "";
    private String category = "";
    //private String page="1";
    private Integer page = 1;

    private ProgressDialog progressDialog;
    private NewsData newsData;
    private List<SingleNews> newsList;
    private RecyclerView recyclerView;
    private MyNewsListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    //private SwipeRefreshLayout swipeRefreshLayout;
    private EasyRefreshLayout easyRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_news_list, container, false);

        // init search options
        Bundle bundle = getArguments();
        words = bundle.getString("words");
        category = bundle.getString("category");
        page = 1;
        initView();
        request();
        System.out.println("new build fragment news list: " + words + " " + category);
        return view;
    }

    public void initView(){
        recyclerView = view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        newsList = new ArrayList<SingleNews>();
        mAdapter = new MyNewsListAdapter(newsList, getActivity());
        recyclerView.setAdapter(mAdapter);

        easyRefreshLayout = view.findViewById(R.id.easy_refresh_layout);
        easyRefreshLayout.addEasyEvent(new EasyRefreshLayout.EasyEvent() {
            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page ++;
                        request();
                        easyRefreshLayout.loadMoreComplete();
                        Toast.makeText(getActivity(), "Yeah, load more !!!!", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }

            @Override
            public void onRefreshing() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        newsList.clear();
                        page = 1;
                        request();
                        easyRefreshLayout.refreshComplete();
                        Toast.makeText(getActivity(), "Yeah, refreshed !!!!", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });
    }

    // TODO: 让加载操作位于新线程中
    public void request(){
        //progressDialog = new ProgressDialog(getActivity());
        //progressDialog.setMessage("Loading....");
        //progressDialog.show();
        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Call<NewsData> call;
        call = service.getNewsSearch(
                this.size,
                this.startDate,
                this.endDate,
                this.words,
                this.category,
                this.page.toString()
        );
        call.enqueue(new Callback<NewsData>() {
            @Override
            public void onResponse(Call<NewsData> call, Response<NewsData> response) {
                //progressDialog.dismiss();
                newsList.addAll(response.body().getData());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<NewsData> call, Throwable t) {
                // progressDialog.dismiss();
                // TODO: load error activity
                Toast.makeText(getActivity(), "Load error.... maybe retry....", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
