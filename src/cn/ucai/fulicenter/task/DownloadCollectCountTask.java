package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Contact;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/6/20.
 */
public class DownloadCollectCountTask extends BaseActivity{
    public static final String TAG = "DownloadCollectCountTask";
    Context context;
    String path;
    String name;

    public DownloadCollectCountTask(Context context) {
        this.context = context;
        this.name = FuliCenterApplication.getInstance().getUser().getMUserName();
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Collect.USER_NAME, name)
                    .getRequestUrl(I.REQUEST_FIND_COLLECT_COUNT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<MessageBean>(path,MessageBean.class,
                responseDownloadContactlistListener(),errorListener()));
    }

    private Response.Listener<MessageBean> responseDownloadContactlistListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean contacts) {
                if (contacts != null) {
                    String msg = contacts.getMsg();
                    FuliCenterApplication.getInstance().setCollectCount(Integer.parseInt(msg));
                } else {
                    FuliCenterApplication.getInstance().setCollectCount(0);
                }

                context.sendStickyBroadcast(new Intent("update_collect_count"));
            }
        };
    }
}
