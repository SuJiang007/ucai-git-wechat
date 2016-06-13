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
public class DownloadAllGroupTask extends BaseActivity {
    public static final String TAG = "DownloadPublicGroupTask";
    String username;
    String path;
    Context mContext;

    public DownloadAllGroupTask(String username, Context mContext) {
        this.username = username;
        this.mContext = mContext;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME, username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,
                reponseDwnloadAllGroupListener(),errorListener()));
    }

    private Response.Listener<Group[]> reponseDwnloadAllGroupListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                if (groups != null) {
                    ArrayList<Group> groupArrayList = SuperWeChatApplication.getInstance().getGroupArrayList();
                    ArrayList<Group> list = Utils.array2List(groups);
                    groupArrayList.clear();
                    groupArrayList.addAll(list);
                    mContext.sendStickyBroadcast(new Intent("update_group_list"));
                }
            }
        };
    }
}
