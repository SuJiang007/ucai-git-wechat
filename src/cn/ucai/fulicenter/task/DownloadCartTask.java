package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;

/**
 * Created by Administrator on 2016/6/22.
 */
public class DownloadCartTask extends BaseActivity{
    private Context mContext;
    private ArrayList<CartBean> cartlist;
    private CartBean cartBean;
    String path;
    int CartType = 0;

    public DownloadCartTask(Context mContext, CartBean cartBean) {
        this.mContext = mContext;
        this.cartBean = cartBean;
        initPath();
    }

    private void initPath() {
        cartlist = FuliCenterApplication.getInstance().getCartBeanArrayList();
        try {
            if (cartlist.contains(cartBean)) {
                if (cartBean.getCount() <= 0) {
                    path = new ApiParams()
                            .with(I.Cart.ID, cartBean.getId() + "")
                            .getRequestUrl(I.REQUEST_DELETE_CART);
                    CartType = 0;
                } else {
                    path = new ApiParams()
                            .with(I.Cart.ID, cartBean.getId() + "")
                            .with(I.Cart.COUNT, cartBean.getCount() + "")
                            .with(I.Cart.IS_CHECKED, cartBean.isChecked() + "")
                            .getRequestUrl(I.REQUEST_UPDATE_CART);
                    CartType = 1;
                }
            } else {
                path = new ApiParams()
                        .with(I.Cart.USER_NAME, cartBean.getUserName())
                        .with(I.Cart.GOODS_ID, cartBean.getGoodsId() + "")
                        .with(I.Cart.COUNT, cartBean.getCount() + "")
                        .with(I.Cart.IS_CHECKED, cartBean.isChecked() + "")
                        .getRequestUrl(I.REQUEST_ADD_CART);
                CartType = 2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<MessageBean>(path,MessageBean.class,
                responseUpdateCartListener(),errorListener()));
    }

    private Response.Listener<MessageBean> responseUpdateCartListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (CartType == 0) {
                    cartlist.remove(cartBean);
                }
                if (CartType == 1) {
                    cartlist.set(cartlist.indexOf(cartBean), cartBean);
                }
                if (CartType == 2) {
                    cartBean.setId(Integer.parseInt(messageBean.getMsg()));
                    cartlist.add(cartBean);
                }
                mContext.sendStickyBroadcast(new Intent("update_cart"));
            }
        };
    }
}
