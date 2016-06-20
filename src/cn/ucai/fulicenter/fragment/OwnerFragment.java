package cn.ucai.fulicenter.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.UserUtils;
import cn.ucai.fulicenter.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class OwnerFragment extends Fragment {

    NetworkImageView userAvatar;
    TextView mtv_name,mtv_collect_count,mtv_Setting;
    ImageView miv_Buy_Baby,miv_kajuanbao,miv_shenghuojuan,miv_wangdianjuan,miv_huiyuanka,miv_wodetequan;
    GridView mgv;
    Context mContext;
    int collectCount;
    ImageView miv_message;

    User user;

    public OwnerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getActivity();
        View layout = inflater.inflate(R.layout.fragment_owner, container, false);
        updateCollectChanged();
        updateUserChanged();
        initView(layout);
        initDate();
        return layout;
    }

    private void initDate() {
        collectCount = FuliCenterApplication.getInstance().getCollectCount();
        mtv_collect_count.setText(""+collectCount);
        user = FuliCenterApplication.getInstance().getUser();
        if (user != null) {
            UserUtils.setCurrentUserAvatar(userAvatar);
            UserUtils.setCurrentUserNick(mtv_name);
        }
    }

    private void initView(View layout) {
        userAvatar = (NetworkImageView) layout.findViewById(R.id.owner_photo);
        mtv_name = (TextView) layout.findViewById(R.id.owner_name);
        mtv_collect_count = (TextView) layout.findViewById(R.id.owner_collect_count);
        mtv_Setting = (TextView) layout.findViewById(R.id.owner_set);
        miv_Buy_Baby = (ImageView) layout.findViewById(R.id.buy_baobei);
        miv_kajuanbao = (ImageView) layout.findViewById(R.id.kajuanbao);
        miv_shenghuojuan = (ImageView) layout.findViewById(R.id.shenghuojuan);
        miv_wangdianjuan = (ImageView) layout.findViewById(R.id.wangdianjuan);
        miv_huiyuanka = (ImageView) layout.findViewById(R.id.huiyuanka);
        miv_wodetequan = (ImageView) layout.findViewById(R.id.wodetequan);
        miv_message = (ImageView) layout.findViewById(R.id.owner_msg);

        initOrderList(layout);
    }

    private void initOrderList(View layout) {
        mgv = (GridView) layout.findViewById(R.id.center_user_gridview);
        ArrayList<HashMap<String, Object>> imaglist = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("image", R.drawable.order_list1);
        imaglist.add(map1);
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map2.put("image", R.drawable.order_list2);
        imaglist.add(map2);
        HashMap<String, Object> map3 = new HashMap<String, Object>();
        map3.put("image", R.drawable.order_list3);
        imaglist.add(map3);
        HashMap<String, Object> map4 = new HashMap<String, Object>();
        map4.put("image", R.drawable.order_list4);
        imaglist.add(map4);
        HashMap<String, Object> map5 = new HashMap<String, Object>();
        map5.put("image", R.drawable.order_list5);
        imaglist.add(map5);
        SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, imaglist, R.layout.simple_grid_item, new String[]{"image"}
                , new int[]{R.id.image_siplme});
        mgv.setAdapter(simpleAdapter);
    }

    class CollectCountChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            initDate();
        }
    }
    CollectCountChangedReceiver mReceiver;
    private void updateCollectChanged() {
        mReceiver = new CollectCountChangedReceiver();
        IntentFilter filter = new IntentFilter("update_collect_count");
        mContext.registerReceiver(mReceiver, filter);
    }

    class UpdateUserChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            new DownloadCollectCountTask(user.getMUserName(), mContext).execute();
            initDate();
        }
    }
    UpdateUserChangedReceiver mUserReceiver;

    private void updateUserChanged() {
        mUserReceiver = new UpdateUserChangedReceiver();
        IntentFilter filter = new IntentFilter("update_user");
        mContext.registerReceiver(mUserReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
        if (mUserReceiver != null) {
            mContext.unregisterReceiver(mUserReceiver);
        }
    }
}
