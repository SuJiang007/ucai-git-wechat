package cn.ucai.git.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.git.I;
import cn.ucai.git.SuperWeChatApplication;
import cn.ucai.git.activity.BaseActivity;
import cn.ucai.git.bean.Contact;
import cn.ucai.git.data.ApiParams;
import cn.ucai.git.data.GsonRequest;
import cn.ucai.git.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadContactListTask extends BaseActivity {
    public static final String TAG = "DownloadContactListTask";
    String username;
    Context context;
    String path;

    public DownloadContactListTask(String username, Context context) {
        this.username = username;
        this.context = context;
        initPath();
    }

    private void initPath() {
        try {
           path = new ApiParams()
                    .with(I.Contact.USER_NAME, username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Contact[]>(path,Contact[].class,
                responseDownloadContactlistListener(),errorListener()));
    }

    private Response.Listener<Contact[]> responseDownloadContactlistListener() {
        return new Response.Listener<Contact[]>() {
            @Override
            public void onResponse(Contact[] contacts) {
                if (contacts != null) {
                    ArrayList<Contact> contactArrayList = SuperWeChatApplication.getInstance().getContactArrayList();
                    ArrayList<Contact> list = Utils.array2List(contacts);
                    contactArrayList.clear();
                    contactArrayList.addAll(list);
                    HashMap<String, Contact> map = SuperWeChatApplication.getInstance().getMap();
                    map.clear();
                    for (Contact c : list) {
                        map.put(c.getMContactCname(), c);
                    }
                    context.sendStickyBroadcast(new Intent("update_contact_list"));
                }
            }
        };
    }

}
