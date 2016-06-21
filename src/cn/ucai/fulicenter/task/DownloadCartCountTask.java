package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/6/21.
 */
public class DownloadCartCountTask extends BaseActivity{
    public static final String TAG = "DownloadCollectCountTask";
    Context context;
    String path;
    int page_id;
    int page_size;
    ArrayList<CartBean> list;

    public DownloadCartCountTask(Context context,int page_id,int page_size) {
        this.context = context;
        this.page_id = page_id;
        this.page_size = page_size;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Cart.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                    .with(I.PAGE_ID,page_id+"")
                    .with(I.PAGE_SIZE,page_size+"")
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<CartBean[]>(path,CartBean[].class,
                responseDownloadContactlistListener(),errorListener()));
    }

    private Response.Listener<CartBean[]> responseDownloadContactlistListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBeen) {
                if (cartBeen != null) {
                    list = Utils.array2List(cartBeen);
                    for (CartBean cart : list) {
                        try {
                            String path = new ApiParams()
                                    .with(I.CategoryGood.GOODS_ID, cart.getGoodsId() + "")
                                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
                            executeRequest(new GsonRequest<GoodDetailsBean>(path,GoodDetailsBean.class,
                                    responseDownloadGoodDetailListener(cart),errorListener()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                }
            }
        };
    }

    int listsize = 0;
    private Response.Listener<GoodDetailsBean> responseDownloadGoodDetailListener(final CartBean cart) {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                listsize++;
                if (goodDetailsBean != null) {
                    cart.setGoods(goodDetailsBean);
                    ArrayList<CartBean> cartlist = FuliCenterApplication.getInstance().getCartBeanArrayList();
                    if (!cartlist.contains(cart)) {
                        cartlist.add(cart);
                    }
                }
                if (listsize == list.size()) {
                    context.sendStickyBroadcast(new Intent("update_cart_list"));
                }
            }
        };
    }
}
