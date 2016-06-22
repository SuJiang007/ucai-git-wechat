package cn.ucai.fulicenter.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.adapter.CartAdapter;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCartCountTask;
import cn.ucai.fulicenter.task.DownloadCartTask;
import cn.ucai.fulicenter.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CarFragment extends Fragment {

    TextView mtv_cart_heji, mtv_cart_jiesheng;
    Button mbt_kanshou;
    RecyclerView mRecyclerView;
    ArrayList<CartBean> mArraylist;
    FuliCenterMainActivity mContext;
    LinearLayoutManager manager;
    CartAdapter mAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView mtv_refreshHint;
    int action = I.ACTION_PULL_DOWN;
    int page_id = 0;
    String path;


    public CarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = (FuliCenterMainActivity) getActivity();
        UpdateCartListener();
        View layout = inflater.inflate(R.layout.fragment_car, container, false);
        initView(layout);
        setListener();
        initData();
        return layout;
    }

    private void setListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int Refresh_page_id = 0;
                mtv_refreshHint.setVisibility(View.VISIBLE);
                action = I.ACTION_PULL_DOWN;
                try {
                    path = getPath(Refresh_page_id);
                    ((FuliCenterMainActivity) getActivity()).executeRequest(new GsonRequest<CartBean[]>(path
                            , CartBean[].class, responseNewGoodsListener(),
                            mContext.errorListener()));
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
                        page_id += I.PAGE_SIZE_DEFAULT;
                        Log.i("main", "page_id=" + page_id);
                        action = I.ACTION_PULL_UP;
                        path = getPath(page_id);
                        mContext.executeRequest(new GsonRequest<CartBean[]>(path
                                , CartBean[].class, responseNewGoodsListener()
                                , mContext.errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                topposition = recyclerView == null || recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topposition >= 0);
            }
        });
    }

    private void initData() {
        sumPrice();
        try {
            ArrayList<CartBean> mCartList = FuliCenterApplication.getInstance().getCartBeanArrayList();
            mArraylist.clear();
            mArraylist.addAll(mCartList);
            mAdapter.notifyDataSetChanged();
            path = getPath(page_id);
            mContext.executeRequest(new GsonRequest<CartBean[]>(path
                    , CartBean[].class, responseNewGoodsListener(), mContext.errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPath(int page_id) throws Exception {
        String requestUrl = new ApiParams()
                .with(I.Cart.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                .with(I.PAGE_ID, page_id + "")
                .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                .getRequestUrl(I.REQUEST_FIND_CARTS);
        return requestUrl;
    }

    ArrayList<CartBean> cartlist;

    private Response.Listener<CartBean[]> responseNewGoodsListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBeen) {
                if (cartBeen != null) {
                    mAdapter.setMore(true);
                    swipeRefreshLayout.setRefreshing(false);
                    mtv_refreshHint.setVisibility(View.GONE);
                    cartlist = Utils.array2List(cartBeen);
                    for (CartBean cart : cartlist) {
                        try {
                            String path = new ApiParams()
                                    .with(I.CategoryGood.GOODS_ID, cart.getGoodsId() + "")
                                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
                            mContext.executeRequest(new GsonRequest<GoodDetailsBean>(path, GoodDetailsBean.class,
                                    responseDownloadGoodDetailListener(cart), mContext.errorListener()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (cartBeen.length < I.PAGE_SIZE_DEFAULT) {
                        mAdapter.setMore(false);
                    }
                }
            }
        };
    }

    private Response.Listener<GoodDetailsBean> responseDownloadGoodDetailListener(final CartBean cart) {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                if (goodDetailsBean != null) {
                    cart.setGoods(goodDetailsBean);
                    ArrayList<CartBean> cartlist = FuliCenterApplication.getInstance().getCartBeanArrayList();
                    if (!cartlist.contains(cart)) {
                        cartlist.add(cart);
                    }
                    if (action == I.ACTION_PULL_DOWN || action == I.ACTION_DOWNLOAD) {
                        mAdapter.initList(cartlist);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addList(cartlist);
                    }
                }
            }
        };
    }

    private void initView(View layout) {
        mtv_cart_heji = (TextView) layout.findViewById(R.id.cart_heji);
        mtv_cart_jiesheng = (TextView) layout.findViewById(R.id.cart_jiesheng);
        mbt_kanshou = (Button) layout.findViewById(R.id.buy_cart);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.recyclerview_cart);
        manager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(manager);
        mArraylist = new ArrayList<>();
        mAdapter = new CartAdapter(mContext, mArraylist);
        mRecyclerView.setAdapter(mAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.srl_cart);
        mtv_refreshHint = (TextView) layout.findViewById(R.id.tvRefreshHint_cart);
    }


    public void sumPrice() {
        ArrayList<CartBean> arrayList = FuliCenterApplication.getInstance().getCartBeanArrayList();
        int sumPrice = 0;
        int currentPrice = 0;
        if (arrayList != null && arrayList.size() > 0) {
            for (CartBean cart : arrayList) {
                GoodDetailsBean goods = cart.getGoods();
                Log.i("my", "goods=" + goods.toString());
                if (goods != null && cart.isChecked()) {
                    sumPrice += convertprice(goods.getRankPrice()) * cart.getCount();
                    currentPrice += convertprice(goods.getCurrencyPrice()) * cart.getCount();
                }
            }
        }
        int savePrice = currentPrice - sumPrice;
        Log.i("my", "sumprice=" + sumPrice);
        mtv_cart_heji.setText("合计: ¥"+sumPrice);
        mtv_cart_jiesheng.setText("节省: ¥"+savePrice);
    }
    private int convertprice(String price) {
        price = price.substring(price.indexOf("￥") + 1);
        int p1 = Integer.parseInt(price);
        return p1;
    }

    class UpdateCartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    }

    UpdateCartReceiver mReceiver;

    private void UpdateCartListener() {
        mReceiver = new UpdateCartReceiver();
        IntentFilter filter = new IntentFilter("update_cart");
        mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }
}
