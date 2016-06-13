package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.SuperWeChatApplication;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Member;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/5/31.
 */
public class DownloadGroupMemberTask extends BaseActivity {
    public static final String TAG = "DownloadPublicGroupTask";
    String hxid;
    String path;
    Context mContext;

    public DownloadGroupMemberTask(String hxid, Context mContext) {
        this.hxid = hxid;
        this.mContext = mContext;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Member.GROUP_ID, hxid)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void execute() {
        executeRequest(new GsonRequest<Member[]>(path,Member[].class,
                reponseDwnloadAllGroupListener(),errorListener()));
    }

    private Response.Listener<Member[]> reponseDwnloadAllGroupListener() {
        return new Response.Listener<Member[]>() {
            @Override
            public void onResponse(Member[] members) {
                if (members != null) {
                    HashMap<String, ArrayList<Member>> groupmember = SuperWeChatApplication.getInstance().getGroupmember();
                    ArrayList<Member> arrayList = groupmember.get(hxid);
                    ArrayList<Member> list = Utils.array2List(members);
                    if (arrayList != null) {
                        arrayList.clear();
                        arrayList.addAll(list);
                    } else {
                        groupmember.put(hxid, list);
                    }
                    mContext.sendStickyBroadcast(new Intent("update_members_list"));
                }
            }
        };
    }
}
