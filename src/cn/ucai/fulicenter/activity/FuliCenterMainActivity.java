package cn.ucai.fulicenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;


import com.easemob.chat.EMChat;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.fragment.BoutiqueFragment;
import cn.ucai.fulicenter.fragment.CarFragment;
import cn.ucai.fulicenter.fragment.CategoryFragment;
import cn.ucai.fulicenter.fragment.NewGoodsFragment;
import cn.ucai.fulicenter.fragment.OwnerFragment;
import cn.ucai.fulicenter.task.DownloadCartCountTask;
import cn.ucai.fulicenter.utils.UserUtils;
import cn.ucai.fulicenter.utils.Utils;

public class FuliCenterMainActivity extends BaseActivity {

    private RadioButton[] Radio;
    private int index;
    private int currentTabIndex;
    NewGoodsFragment newGoodsFragment;
    BoutiqueFragment boutiqueFragment;
    CategoryFragment categoryFragment;
    CarFragment carFragment;
    OwnerFragment ownerFragment;
    Fragment[] fragments;
    TextView mtv_CartCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuli_center_main);
        registerReceiver();
        initView();
        newGoodsFragment = new NewGoodsFragment();
        boutiqueFragment = new BoutiqueFragment();
        categoryFragment = new CategoryFragment();
        carFragment = new CarFragment();
        ownerFragment = new OwnerFragment();
        fragments = new Fragment[]{newGoodsFragment, boutiqueFragment, categoryFragment, carFragment, ownerFragment};
        // 添加显示第一个fragment

        getSupportFragmentManager().beginTransaction().add(R.id.fl, newGoodsFragment)
                .add(R.id.fl, boutiqueFragment).hide(boutiqueFragment)
                .add(R.id.fl, categoryFragment).hide(categoryFragment)
                .add(R.id.fl, carFragment).hide(carFragment)
//                .add(R.id.fl, ownerFragment).hide(ownerFragment)
                .show(newGoodsFragment)
                .commit();
    }


    /**
     * 初始化组件
     */
    private void initView() {
        mtv_CartCount = (TextView) findViewById(R.id.cart_count);
        Radio = new RadioButton[5];
        Radio[0] = (RadioButton) findViewById(R.id.newGoods);
        Radio[1] = (RadioButton) findViewById(R.id.Boutique);
        Radio[2] = (RadioButton) findViewById(R.id.Category);
        Radio[3] = (RadioButton) findViewById(R.id.tvCar);
        Radio[4] = (RadioButton) findViewById(R.id.me);
        Radio[0].setSelected(true);

        registerForContextMenu(Radio[1]);
    }

    /**
     * Button
     *
     * @param view
     */
    String ACTION_PERSON = "person";
    String ACTION_CART = "cart";

    public void onCheckedChange(View view) {
        switch (view.getId()) {
            case R.id.newGoods:
                index = 0;
                break;
            case R.id.Boutique:
                index = 1;
                break;
            case R.id.Category:
                index = 2;
                break;
            case R.id.tvCar:
                if (FuliCenterApplication.getInstance().getUser() == null) {
                    startActivity(new Intent(this, LoginActivity.class)
                            .putExtra("action", ACTION_CART));
                } else {
                    index = 3;
                }
                break;
            case R.id.me:
                if (FuliCenterApplication.getInstance().getUser() == null) {
                    startActivity(new Intent(this, LoginActivity.class)
                            .putExtra("action", ACTION_PERSON));
                } else {
                    index = 4;
                }
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fl, fragments[index]);
            }
            trx.show(fragments[index]).commit();
            Radio[currentTabIndex].setChecked(false);
            // 把当前tab设为选中状态
            Radio[index].setChecked(true);
            currentTabIndex = index;
        }
    }

    private void setRadioChecked(int index) {
        for (int i = 0; i < Radio.length; i++) {
            if (i == index) {
                Radio[i].setChecked(true);
            } else {
                Radio[i].setChecked(false);
            }
        }
    }

    String action;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        action = getIntent().getStringExtra("action");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    class CartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (FuliCenterApplication.getInstance().getUser() != null) {
                new DownloadCartCountTask(context, 0, 10).execute();
                int SumCount = Utils.SumCount();
                if (SumCount > 0) {
                    int size = FuliCenterApplication.getInstance().getCartBeanArrayList().size();
                    mtv_CartCount.setText("" + size);
                    mtv_CartCount.setVisibility(View.VISIBLE);
                } else {
                    mtv_CartCount.setVisibility(View.GONE);
                }
            }
        }
    }

    CartReceiver mReceiver;

    private void registerReceiver() {
        mReceiver = new CartReceiver();
        IntentFilter filter = new IntentFilter("update_cart_list");
        filter.addAction("update_user");
        filter.addAction("update_cart");
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (action != null && FuliCenterApplication.getInstance().getUser() != null) {
            if (action.equals("person")) {
                index = 4;
            }
            if (action.equals(ACTION_CART)) {
                index = 3;
            }
        } else {
            setRadioChecked(index);
        }
        if (currentTabIndex == 4 && FuliCenterApplication.getInstance().getUser() == null) {
            index = 0;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fl, fragments[index]);
            }
            trx.show(fragments[index]).commit();
            Radio[currentTabIndex].setChecked(false);
            // 把当前tab设为选中状态
            Radio[index].setChecked(true);
            currentTabIndex = index;
        }
    }

}
