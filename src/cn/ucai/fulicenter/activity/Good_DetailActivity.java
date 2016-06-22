package cn.ucai.fulicenter.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import java.io.Serializable;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FlowIndicator;
import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.SlideAutoLoopView;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.UserUtils;
import cn.ucai.fulicenter.utils.Utils;

public class Good_DetailActivity extends BaseActivity {
    TextView mtv_English_Name, mtv_Chinese_Name, mtv_Price;
    WebView wb_Brief;
    SlideAutoLoopView mSlideAutoLoopView;
    GoodDetailsBean mGoods;
    ImageView miv_back, miv_collect;
    FlowIndicator mFlowIndicator;
    int mCurrentColo = 0;
    int GoodsId;
    boolean isRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good__detail);
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        miv_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = FuliCenterApplication.getInstance().getUser();
                if (user == null) {
                    startActivity(new Intent(Good_DetailActivity.this, LoginActivity.class));
                } else {
                    if (isRight) {
                        try {
                            String path = new ApiParams()
                                    .with(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                                    .with(I.Collect.GOODS_ID, GoodsId + "")
                                    .getRequestUrl(I.REQUEST_DELETE_COLLECT);
                            executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                                    responseDeleteCollectListener(), errorListener()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            String url = new ApiParams()
                                    .with(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                                    .with(I.Collect.GOODS_ID, mGoods.getGoodsId() + "")
                                    .with(I.Collect.GOODS_NAME, mGoods.getGoodsName())
                                    .with(I.Collect.GOODS_ENGLISH_NAME, mGoods.getGoodsEnglishName())
                                    .with(I.Collect.GOODS_THUMB, mGoods.getGoodsThumb())
                                    .with(I.Collect.GOODS_IMG, mGoods.getGoodsImg())
                                    .with(I.Collect.ADD_TIME, mGoods.getAddTime() + "")
                                    .getRequestUrl(I.REQUEST_ADD_COLLECT);
                            executeRequest(new GsonRequest<MessageBean>(url,MessageBean.class,
                                    responseAddCollectListener(),errorListener()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        setCartListener();
        RegisterUpdate();
    }

    private void setCartListener() {
        miv_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.addCart(Good_DetailActivity.this,mGoods);
            }
        });
    }

    private Response.Listener<MessageBean> responseAddCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    new DownloadCollectCountTask(Good_DetailActivity.this).execute();
                    isRight = true;
                    miv_collect.setImageResource(R.drawable.bg_collect_out);
                    Toast.makeText(Good_DetailActivity.this,messageBean.getMsg(),Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(Good_DetailActivity.this, messageBean.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.Listener<MessageBean> responseDeleteCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    isRight = false;
                    miv_collect.setImageResource(R.drawable.bg_collect_in);
                    new DownloadCollectCountTask(Good_DetailActivity.this).execute();
                    Toast.makeText(Good_DetailActivity.this, messageBean.getMsg(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Good_DetailActivity.this, messageBean.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void initData() {
        GoodsId = getIntent().getIntExtra(D.NewGood.KEY_GOODS_ID, 0);
        try {
            String path = new ApiParams()
                    .with(D.NewGood.KEY_GOODS_ID, GoodsId + "")
                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
            executeRequest(new GsonRequest<GoodDetailsBean>(path, GoodDetailsBean.class,
                    responseGoodDetailsListener(), errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<GoodDetailsBean> responseGoodDetailsListener() {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                if (goodDetailsBean != null) {
                    mGoods = goodDetailsBean;
                    mtv_English_Name.setText(mGoods.getGoodsEnglishName());
                    mtv_Chinese_Name.setText(mGoods.getGoodsName());
                    mtv_Price.setText(mGoods.getCurrencyPrice());
                    wb_Brief.loadDataWithBaseURL(null, mGoods.getGoodsBrief().trim(), D.TEXT_HTML, D.UTF_8, null);
                    miv_back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

                    //设置图片轮播
                    initColorsBanner();
                } else {
                    Toast.makeText(Good_DetailActivity.this, "下载商品详情失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        };
    }

    private void initColorsBanner() {
        //设置第一个颜色的图片轮播
        updateColor(0);
        for (int i = 0; i < mGoods.getProperties().length; i++) {
            mCurrentColo = i;
            View layout = View.inflate(this, R.layout.layout_property_color, null);
            NetworkImageView ivColor = (NetworkImageView) layout.findViewById(R.id.ivColorItem);
            String colorImg = mGoods.getProperties()[i].getColorImg();
            if (colorImg.isEmpty()) {
                continue;
            }
            ImageUtils.setGoodDetailThumb(colorImg, ivColor);

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateColor(mCurrentColo);
                }
            });
        }
    }

    private void updateColor(int i) {
        AlbumBean[] album = mGoods.getProperties()[i].getAlbums();
        String[] albumsImgUrl = new String[album.length];
        for (int j = 0; j < albumsImgUrl.length; j++) {
            albumsImgUrl[j] = album[j].getImgUrl();
        }
        mSlideAutoLoopView.startPlayLoop(mFlowIndicator, albumsImgUrl, albumsImgUrl.length);
    }

    ImageView miv_cart;
    TextView mtv_CartCount;
    private void initView() {
        mtv_CartCount = (TextView) findViewById(R.id.count);
        miv_cart = (ImageView) findViewById(R.id.cart);
        mFlowIndicator = (FlowIndicator) findViewById(R.id.flowIndicator);
        mtv_English_Name = (TextView) findViewById(R.id.English_name);
        mtv_Chinese_Name = (TextView) findViewById(R.id.Chinese_name);
        mtv_Price = (TextView) findViewById(R.id.price);
        wb_Brief = (WebView) findViewById(R.id.wvGoodBrief);
        mSlideAutoLoopView = (SlideAutoLoopView) findViewById(R.id.salv);
        miv_back = (ImageView) findViewById(R.id.back);
        miv_collect = (ImageView) findViewById(R.id.collect);
        WebSettings settings = wb_Brief.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setBuiltInZoomControls(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCollect();
        initCartStatus();
    }
    private void initCartStatus() {
        int count = Utils.SumCount();
        if (count > 0) {
            mtv_CartCount.setVisibility(View.VISIBLE);
            mtv_CartCount.setText("" + count);
        } else {
            mtv_CartCount.setVisibility(View.GONE);
            mtv_CartCount.setText("0");
        }
    }
    private void initCollect() {
        try {
            String path = new ApiParams()
                    .with(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                    .with(I.Collect.GOODS_ID, GoodsId + "")
                    .getRequestUrl(I.REQUEST_IS_COLLECT);
            executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                    responseIsCollectListener(), errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<MessageBean> responseIsCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    miv_collect.setImageResource(R.drawable.bg_collect_out);
                    isRight = true;
                } else {
                    miv_collect.setImageResource(R.drawable.bg_collect_in);
                    isRight = false;
                }
            }
        };
    }

    class UpdateCartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            initCartStatus();
        }
    }

    UpdateCartReceiver mReceiver;

    private void RegisterUpdate() {
        mReceiver = new UpdateCartReceiver();
        IntentFilter filter = new IntentFilter("update_cart");
        registerReceiver(mReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}
