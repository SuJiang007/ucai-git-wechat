package cn.ucai.fulicenter.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.adapter.BoutiqueAdapter;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class BoutiqueFragment extends Fragment {

    SwipeRefreshLayout mswiprefresh;
    TextView mtv_tvrefresh;
    RecyclerView mRecyclerView;
    ArrayList<BoutiqueBean> mArrayList;
    BoutiqueAdapter mAdapter;
    FuliCenterMainActivity mContext;
    LinearLayoutManager manager;
    int action = I.ACTION_DOWNLOAD;
    String path;

    public BoutiqueFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = (FuliCenterMainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_boutique, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        setListener();
    }
    private void initData() {
        try {
            path = getPath();
            ((FuliCenterMainActivity)getActivity()).executeRequest(new GsonRequest<BoutiqueBean[]>(path
                    ,BoutiqueBean[].class,responseNewGoodsListener(),((FuliCenterMainActivity)getActivity()).errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setListener() {
        mswiprefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mtv_tvrefresh.setVisibility(View.VISIBLE);
                action = I.ACTION_PULL_DOWN;
                try {
                    ((FuliCenterMainActivity)getActivity()).executeRequest(new GsonRequest<BoutiqueBean[]>(path
                            ,BoutiqueBean[].class,responseNewGoodsListener(),
                            ((FuliCenterMainActivity)getActivity()).errorListener()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int topposition;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastposition = manager.findLastVisibleItemPosition();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastposition >= mAdapter.getItemCount() - 1 && mAdapter.isMore) {
                    try {
                        action = I.ACTION_PULL_UP;
                        ((FuliCenterMainActivity)getActivity()).executeRequest(new GsonRequest<BoutiqueBean[]>(path
                                ,BoutiqueBean[].class,responseNewGoodsListener()
                                ,((FuliCenterMainActivity)getActivity()).errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                topposition = recyclerView == null || recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                mswiprefresh.setEnabled(topposition>=0);
            }
        });
    }

    private Response.Listener<BoutiqueBean[]> responseNewGoodsListener() {
        return new Response.Listener<BoutiqueBean[]>() {
            @Override
            public void onResponse(BoutiqueBean[] newGoodBeen) {
                if (newGoodBeen != null) {
                    mAdapter.setMore(true);
                    mswiprefresh.setRefreshing(false);
                    mtv_tvrefresh.setVisibility(View.GONE);
                    mAdapter.setFootText(getResources().getString(R.string.load_more));
                    ArrayList<BoutiqueBean> newBean = Utils.array2List(newGoodBeen);
                    if (action == I.ACTION_PULL_DOWN || action == I.ACTION_DOWNLOAD) {
                        mAdapter.initList(newBean);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addList(newBean);
                    }
                    if (newGoodBeen.length < I.PAGE_SIZE_DEFAULT) {
                        mAdapter.setFootText(getResources().getString(R.string.no_more));
                        mAdapter.setMore(false);
                    }
                }
            }
        };
    }
    private String getPath() throws Exception {
        String requestUrl = "http://10.0.2.2:9999/FuLiCenterServer/Server?request=find_boutiques";
        Log.i("main", "requestUrl=" + requestUrl);
        return requestUrl;
    }
    private void initView() {
        mswiprefresh = (SwipeRefreshLayout) mContext.findViewById(R.id.swiprefresh_boutique);
        mtv_tvrefresh = (TextView) mContext.findViewById(R.id.tvRefreshHint_boutique);
        mRecyclerView = (RecyclerView) mContext.findViewById(R.id.recyclerview_boutique);
        mArrayList = new ArrayList<>();
        mAdapter = new BoutiqueAdapter(mContext, mArrayList);
        manager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
