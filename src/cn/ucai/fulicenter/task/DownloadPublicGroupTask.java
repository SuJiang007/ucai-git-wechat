package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.SuperWeChatApplication;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Group;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadPublicGroupTask extends BaseActivity{
    public static final String TAG = "DownloadPublicGroupTask";
    int PAGE_ID;
    int PAGE_SIZE;
    String username;
    String path;
    Context mContext;

    public DownloadPublicGroupTask(int PAGE_ID, int PAGE_SIZE, String username, Context mContext) {
        this.PAGE_ID = PAGE_ID;
        this.PAGE_SIZE = PAGE_SIZE;
        this.username = username;
        this.mContext = mContext;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME, username)
                    .with(I.PAGE_ID, PAGE_ID + "")
                    .with(I.PAGE_SIZE, PAGE_SIZE + "")
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUPS);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,
                responseDownloadpublickGroupListener(),errorListener()));
    }

    private Response.Listener<Group[]> responseDownloadpublickGroupListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                if (groups != null) {
                    ArrayList<Group> publicArrayList = SuperWeChatApplication.getInstance().getPublicArrayList();
                    ArrayList<Group> list = Utils.array2List(groups);
                    publicArrayList.clear();
                    publicArrayList.addAll(list);
                    mContext.sendStickyBroadcast(new Intent("update_public_group"));
                }
            }
        };
    }
}
