package cn.ucai.fulicenter.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.CatChildFilterButton;
import cn.ucai.fulicenter.ColorFilterButton;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.Category_DetaiAdapter;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.ColorBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;

public class Category_DetaiActivity extends BaseActivity {

    RecyclerView mRecyclerView;
    ImageView miv_back;
    TextView mtv_shaixuan;
    Category_DetaiAdapter mAdapter;
    ArrayList<NewGoodBean> mArraylist;
    Button mbt_price;
    Button mbt_addtime;

    SwipeRefreshLayout swipeRefreshLayout;
    TextView mtv_refresh;
    GridLayoutManager manager;
    int page_id = 0;
    //定义三种刷新的动作
    int action = I.ACTION_DOWNLOAD;
    String path;
    int goodsId = 0;
    int Sort = I.SORT_BY_ADDTIME_DESC;
    Context mContext;
    CatChildFilterButton catchild;
    String mGroupName;
    ArrayList<CategoryChildBean> arrayList;
    ColorFilterButton mColorFilterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category__detai);
        mContext = this;
        arrayList = new ArrayList<CategoryChildBean>();
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        SortStateChangedListener sortStateChangedListener = new SortStateChangedListener();
        mbt_price.setOnClickListener(sortStateChangedListener);
        mbt_addtime.setOnClickListener(sortStateChangedListener);
        swipPull_Down();
        refresh_PULL_UP();
        catchild.setOnCatFilterClickListener(mGroupName, arrayList);
    }

    private void refresh_PULL_UP() {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int Topposition;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastposition = manager.findLastVisibleItemPosition();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastposition >= mAdapter.getItemCount() - 1 && mAdapter.isMore) {
                    try {
                        page_id += I.PAGE_SIZE_DEFAULT;
                        Log.i("main", "page_id=" + page_id);
                        action = I.ACTION_PULL_UP;
                        path = getPath(page_id);
                        executeRequest(new GsonRequest<NewGoodBean[]>(path
                                , NewGoodBean[].class, responseNewGoodsListener()
                                , errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Topposition = recyclerView == null || recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(Topposition >= 0);
            }
        });
    }

    private void swipPull_Down() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int Refresh_page_id = 0;
                mtv_refresh.setVisibility(View.VISIBLE);
                action = I.ACTION_PULL_DOWN;
                try {
                    path = getPath(Refresh_page_id);
                    executeRequest(new GsonRequest<NewGoodBean[]>(path
                            , NewGoodBean[].class, responseNewGoodsListener(),
                            errorListener()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initData() {
        try {
            arrayList = (ArrayList<CategoryChildBean>) getIntent().getSerializableExtra("childList");
            path = getPath(page_id);
            executeRequest(new GsonRequest<NewGoodBean[]>(path
                    , NewGoodBean[].class, responseNewGoodsListener(), errorListener()));
            String url = new ApiParams()
                    .with(I.Color.CAT_ID, goodsId + "")
                    .getRequestUrl(I.REQUEST_FIND_COLOR_LIST);
            executeRequest(new GsonRequest<ColorBean[]>(url, ColorBean[].class,
                    reponseDownloadColorListener(), errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<ColorBean[]> reponseDownloadColorListener() {
        return new Response.Listener<ColorBean[]>() {
            @Override
            public void onResponse(ColorBean[] colorBeen) {
                ArrayList<ColorBean> list = Utils.array2List(colorBeen);
                if (colorBeen != null) {
                    mColorFilterButton.setVisibility(View.VISIBLE);
                    mColorFilterButton.setOnColorFilterClickListener(mGroupName, arrayList, list);
                }
            }
        };
    }


    private String getPath(int page_id) throws Exception {
        Log.i("main", "page_id=" + page_id);
        goodsId = getIntent().getIntExtra(I.CategoryChild.CAT_ID, 0);
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
        mbt_price = (Button) findViewById(R.id.price_category);
        mbt_addtime = (Button) findViewById(R.id.addtime_category);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_category);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_category);
        miv_back = (ImageView) findViewById(R.id.back_category);
        mtv_shaixuan = (TextView) findViewById(R.id.btn_colorfilter);
        mtv_refresh = (TextView) findViewById(R.id.tv_Refresh_category);
        manager = new GridLayoutManager(this, I.COLUM_NUM);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mArraylist = new ArrayList<NewGoodBean>();
        mAdapter = new Category_DetaiAdapter(this, mArraylist, Sort);
        mRecyclerView.setAdapter(mAdapter);
        mGroupName = getIntent().getStringExtra(I.CategoryGroup.NAME);
        catchild = (CatChildFilterButton) findViewById(R.id.btn_catchildfliter);
        catchild.setText(mGroupName);
        mColorFilterButton = (ColorFilterButton) findViewById(R.id.btn_colorfilter);
        mColorFilterButton.setVisibility(View.VISIBLE);
        miv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //判断按价格排序
    boolean mSortByPriceAsc;
    //判断按上架时间排序
    boolean mSortByAddTimeAsc;

    class SortStateChangedListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Drawable right = null;
            int resId;
            switch (v.getId()) {
                case R.id.price_category:
                    if (mSortByPriceAsc) {
                        Sort = I.SORT_BY_PRICE_ASC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_up);
                        resId = R.drawable.arrow_order_up;
                    } else {
                        Sort = I.SORT_BY_PRICE_DESC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_down);
                        resId = R.drawable.arrow_order_down;
                    }
                    mSortByPriceAsc = !mSortByPriceAsc;
                    right.setBounds(0, 0, ImageUtils.getDrawableWidth(mContext, resId), ImageUtils.getDrawableHeight(mContext, resId));
                    mbt_price.setCompoundDrawables(null, null, right, null);
                    break;
                case R.id.addtime_category:
                    if (mSortByAddTimeAsc) {
                        Sort = I.SORT_BY_ADDTIME_ASC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_up);
                        resId = R.drawable.arrow_order_up;
                    } else {
                        Sort = I.SORT_BY_ADDTIME_DESC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_down);
                        resId = R.drawable.arrow_order_down;
                    }
                    mSortByAddTimeAsc = !mSortByAddTimeAsc;
                    right.setBounds(0, 0, ImageUtils.getDrawableWidth(mContext, resId), ImageUtils.getDrawableHeight(mContext, resId));
                    mbt_addtime.setCompoundDrawables(null, null, right, null);
                    break;
            }
            mAdapter.setSortBy(Sort);
        }
    }
}
