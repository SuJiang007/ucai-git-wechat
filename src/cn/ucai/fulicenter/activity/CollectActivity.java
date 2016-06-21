package cn.ucai.fulicenter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.CollectAdapter;
import cn.ucai.fulicenter.adapter.NewGoodAdapter;
import cn.ucai.fulicenter.bean.CollectBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.fragment.OwnerFragment;
import cn.ucai.fulicenter.utils.Utils;

public class CollectActivity extends BaseActivity {
    int page_id = 0;
    ArrayList<CollectBean> mArratList;
    CollectAdapter mAdapter;
    //定义三种刷新的动作
    int action = I.ACTION_DOWNLOAD;
    RecyclerView mrcont;
    SwipeRefreshLayout mswipe;
    TextView mtvRefreshHint;
    GridLayoutManager manager;
    String path;
    CollectActivity mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        mContext = this;
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        miv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mswipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int Refresh_page_id = 0;
                mtvRefreshHint.setVisibility(View.VISIBLE);
                action = I.ACTION_PULL_DOWN;
                try {
                    path = getPath(Refresh_page_id);
                    executeRequest(new GsonRequest<CollectBean[]>(path
                            ,CollectBean[].class,responseNewGoodsListener(),
                            errorListener()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mrcont.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int topposition;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastposition = manager.findLastVisibleItemPosition();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastposition >= mAdapter.getItemCount() - 1 && mAdapter.isMore) {
//                    mswipe.setRefreshing(true);
                    try {
                        page_id += I.PAGE_SIZE_DEFAULT;
                        Log.i("main", "page_id=" + page_id);
                        action = I.ACTION_PULL_UP;
                        path = getPath(page_id);
                       executeRequest(new GsonRequest<CollectBean[]>(path
                                ,CollectBean[].class,responseNewGoodsListener()
                                ,errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                topposition = recyclerView == null || recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                mswipe.setEnabled(topposition>=0);
            }
        });
    }

    private void initData() {
        try {
            path = getPath(page_id);
            executeRequest(new GsonRequest<CollectBean[]>(path
                    ,CollectBean[].class,responseNewGoodsListener(),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getPath(int page_id) throws Exception {
        Log.i("main", "page_id=" + page_id);
        String requestUrl = new ApiParams()
                .with(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                .with(I.PAGE_ID,page_id+"")
                .with(I.PAGE_SIZE,I.PAGE_SIZE_DEFAULT+"")
                .getRequestUrl(I.REQUEST_FIND_COLLECTS);
        Log.i("main", "url=" + requestUrl);
        return requestUrl;
    }

    private Response.Listener<CollectBean[]> responseNewGoodsListener() {
        return new Response.Listener<CollectBean[]>() {
            @Override
            public void onResponse(CollectBean[] newGoodBeen) {
                if (newGoodBeen != null) {
                    mAdapter.setMore(true);
                    mswipe.setRefreshing(false);
                    mtvRefreshHint.setVisibility(View.GONE);
                    mAdapter.setFootText(getResources().getString(R.string.load_more));
                    ArrayList<CollectBean> newBean = Utils.array2List(newGoodBeen);
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

    ImageView miv_back;
    private void initView() {
        mswipe = (SwipeRefreshLayout) findViewById(R.id.srl_collect);
        mtvRefreshHint = (TextView)findViewById(R.id.tvRefreshHint_collect);
        mrcont = (RecyclerView)findViewById(R.id.rvContact_collect);
        manager = new GridLayoutManager(mContext,I.COLUM_NUM);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mrcont.setHasFixedSize(true);
        mrcont.setLayoutManager(manager);
        mArratList = new ArrayList<CollectBean>();
        mAdapter = new CollectAdapter(mContext, mArratList);
        mrcont.setAdapter(mAdapter);
        miv_back = (ImageView) findViewById(R.id.back_collect);
    }

}
