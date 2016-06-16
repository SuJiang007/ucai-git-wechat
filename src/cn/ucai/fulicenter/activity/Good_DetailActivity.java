package cn.ucai.fulicenter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
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
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.SlideAutoLoopView;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.UserUtils;

public class Good_DetailActivity extends BaseActivity {
    TextView mtv_English_Name,mtv_Chinese_Name,mtv_Price;
    WebView wb_Brief;
    SlideAutoLoopView mSlideAutoLoopView;
    GoodDetailsBean mGoods;
    ImageView miv_back;
    FlowIndicator mFlowIndicator;
    int mCurrentColo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good__detail);
        initView();
        initData();
    }

    private void initData() {
        int goodsid = getIntent().getIntExtra(D.NewGood.KEY_GOODS_ID, 0);
        try {
            String path = new ApiParams()
                    .with(D.NewGood.KEY_GOODS_ID, goodsid + "")
                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
            executeRequest(new GsonRequest<GoodDetailsBean>(path,GoodDetailsBean.class,
                    responseGoodDetailsListener(),errorListener()));
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
                    wb_Brief.loadDataWithBaseURL(null,mGoods.getGoodsBrief().trim(),D.TEXT_HTML,D.UTF_8,null);
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
        for (int i=0;i<mGoods.getProperties().length;i++) {
            mCurrentColo = i;
            View layout = View.inflate(this, R.layout.layout_property_color, null);
            NetworkImageView ivColor = (NetworkImageView) layout.findViewById(R.id.ivColorItem);
            String colorImg = mGoods.getProperties()[i].getColorImg();
            if (colorImg.isEmpty()) {
                continue;
            }
            ImageUtils.setGoodDetailThumb(colorImg,ivColor);

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
        for (int j=0;j<albumsImgUrl.length;j++) {
            albumsImgUrl[j] = album[j].getImgUrl();
        }
        mSlideAutoLoopView.startPlayLoop(mFlowIndicator,albumsImgUrl,albumsImgUrl.length);
    }

    private void initView() {
        mFlowIndicator = (FlowIndicator) findViewById(R.id.flowIndicator);
        mtv_English_Name = (TextView) findViewById(R.id.English_name);
        mtv_Chinese_Name = (TextView) findViewById(R.id.Chinese_name);
        mtv_Price = (TextView) findViewById(R.id.price);
        wb_Brief = (WebView) findViewById(R.id.wvGoodBrief);
        mSlideAutoLoopView = (SlideAutoLoopView) findViewById(R.id.salv);
        miv_back = (ImageView) findViewById(R.id.back);
        WebSettings settings = wb_Brief.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setBuiltInZoomControls(true);
    }

}
