package cn.ucai.fulicenter.activity;

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

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.Boutique_DetailAdapter;
import cn.ucai.fulicenter.adapter.NewGoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

public class Boutique_DetailActivity extends BaseActivity {
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtv_refresh;
    GridLayoutManager manager;
    int page_id = 0;
    ArrayList<NewGoodBean> mArratList;
    Boutique_DetailAdapter mAdapter;
    //定义三种刷新的动作
    int action = I.ACTION_DOWNLOAD;
    String path;
    int goodsId = 0;
    TextView mtv_detai_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boutique__detail);
        goodsId = getIntent().getIntExtra("CatId", 0);
        initView();
        initData();
        setListener();
    }
    private void setListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int Refresh_page_id = 0;
                mtv_refresh.setVisibility(View.VISIBLE);
                action = I.ACTION_PULL_DOWN;
                try {
                    path = getPath(Refresh_page_id);
                   executeRequest(new GsonRequest<NewGoodBean[]>(path
                            ,NewGoodBean[].class,responseNewGoodsListener(),
                            errorListener()));
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
//                    mswipe.setRefreshing(true);
                    try {
                        page_id += I.PAGE_SIZE_DEFAULT;
                        Log.i("main", "page_id=" + page_id);
                        action = I.ACTION_PULL_UP;
                        path = getPath(page_id);
                        executeRequest(new GsonRequest<NewGoodBean[]>(path
                                ,NewGoodBean[].class,responseNewGoodsListener()
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
                swipeRefreshLayout.setEnabled(topposition>=0);
            }
        });
    }

    private void initData() {
        try {
            path = getPath(page_id);
            executeRequest(new GsonRequest<NewGoodBean[]>(path
                    ,NewGoodBean[].class,responseNewGoodsListener(),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getPath(int page_id) throws Exception {
        Log.i("main", "page_id=" + page_id);
        String requestUrl = new ApiParams()
                .with(I.NewAndBoutiqueGood.CAT_ID, goodsId + "")
                .with(I.PAGE_ID, page_id + "")
                .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                .getRequestUrl(I.REQUEST_FIND_NEW_BOUTIQUE_GOODS);
        Log.i("main", "url=" + requestUrl);
        return requestUrl;
    }

    private Response.Listener<NewGoodBean[]> responseNewGoodsListener() {
        return new Response.Listener<NewGoodBean[]>() {
            @Override
            public void onResponse(NewGoodBean[] newGoodBeen) {
                if (newGoodBeen != null) {
                    mAdapter.setMore(true);
                    swipeRefreshLayout.setRefreshing(false);
                    mtv_refresh.setVisibility(View.GONE);
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
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_boutique);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mtv_refresh = (TextView) findViewById(R.id.tv_Refresh_boutique);
        manager = new GridLayoutManager(this,I.COLUM_NUM);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mArratList = new ArrayList<NewGoodBean>();
        mAdapter = new Boutique_DetailAdapter(this, mArratList,I.SORT_BY_ADDTIME_DESC);
        mRecyclerView.setAdapter(mAdapter);
        mtv_detai_name = (TextView) findViewById(R.id.boutique_detail_name);
        mtv_detai_name.setText(getIntent().getStringExtra("boutiquename"));
        ImageView miv_back = (ImageView) findViewById(R.id.back_boutique);
        miv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Boutique_DetailActivity.this,FuliCenterMainActivity.class));
            }
        });
    }
}
