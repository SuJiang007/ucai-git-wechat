package cn.ucai.fulicenter.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.adapter.NewGoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.UserUtils;
import cn.ucai.fulicenter.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewGoodsFragment extends Fragment {


    static int page_id = 0;
    ArrayList<NewGoodBean> mArratList;
    NewGoodAdapter mAdapter;
    //定义三种刷新的动作
    int action = I.ACTION_DOWNLOAD;
    RecyclerView mrcont;
    SwipeRefreshLayout mswipe;
    TextView mtvRefreshHint;
    GridLayoutManager manager;
    String path;
    FuliCenterMainActivity mContext;
    public NewGoodsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = (FuliCenterMainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_new_goods, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        mswipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page_id = 0;
                mtvRefreshHint.setVisibility(View.VISIBLE);
                action = I.ACTION_PULL_DOWN;
                try {
                    getPath(page_id);
                    ((FuliCenterMainActivity)getActivity()).executeRequest(new GsonRequest<NewGoodBean[]>(path
                            ,NewGoodBean[].class,responseNewGoodsListener(),
                            ((FuliCenterMainActivity)getActivity()).errorListener()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mrcont.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastposition;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastposition >= mAdapter.getItemCount() - 1 && mAdapter.isMore) {
                    mswipe.setRefreshing(true);
                    page_id += I.PAGE_SIZE_DEFAULT;
                    action = I.ACTION_PULL_UP;
                    try {
                        getPath(page_id);
                        ((FuliCenterMainActivity)getActivity()).executeRequest(new GsonRequest<NewGoodBean[]>(path
                                ,NewGoodBean[].class,responseNewGoodsListener()
                                ,((FuliCenterMainActivity)getActivity()).errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastposition = manager.findLastVisibleItemPosition();
                mswipe.setEnabled(manager.findFirstCompletelyVisibleItemPosition()==0);
            }
        });
    }

    private void initData() {
        try {
            path = getPath(page_id);
            ((FuliCenterMainActivity)getActivity()).executeRequest(new GsonRequest<NewGoodBean[]>(path
                    ,NewGoodBean[].class,responseNewGoodsListener(),((FuliCenterMainActivity)getActivity()).errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getPath(int page_id) throws Exception {
        return new ApiParams()
                        .with(I.NewAndBoutiqueGood.CAT_ID, I.CAT_ID + "")
                        .with(I.PAGE_ID, page_id + "")
                        .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                        .getRequestUrl(I.REQUEST_FIND_NEW_BOUTIQUE_GOODS);
    }

    private Response.Listener<NewGoodBean[]> responseNewGoodsListener() {
        return new Response.Listener<NewGoodBean[]>() {
            @Override
            public void onResponse(NewGoodBean[] newGoodBeen) {
                if (newGoodBeen != null) {
                    mAdapter.setMore(true);
                    mswipe.setRefreshing(false);
                    mtvRefreshHint.setVisibility(View.GONE);
                    mAdapter.setFootText(getResources().getString(R.string.load_more));
                    ArrayList<NewGoodBean> newBean = Utils.array2List(newGoodBeen);
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

    private void initView() {
        mswipe = (SwipeRefreshLayout) getActivity().findViewById(R.id.srl);
        mtvRefreshHint = (TextView) getActivity().findViewById(R.id.tvRefreshHint);
        mrcont = (RecyclerView) getActivity().findViewById(R.id.rvContact);
        manager = new GridLayoutManager(mContext,I.COLUM_NUM);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mrcont.setHasFixedSize(true);
        mrcont.setLayoutManager(manager);
        mArratList = new ArrayList<NewGoodBean>();
        mAdapter = new NewGoodAdapter(mContext, mArratList);
        mrcont.setAdapter(mAdapter);
    }
}
