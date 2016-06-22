package cn.ucai.fulicenter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.task.DownloadCartTask;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/6/22.
 */
public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    FuliCenterMainActivity context;
    ArrayList<CartBean> arrayList;
    public boolean isMore;
    CartViewHolder cartViewHolder;

    public CartAdapter(FuliCenterMainActivity context, ArrayList<CartBean> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public void initList(ArrayList<CartBean> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            this.arrayList.clear();
        }
        this.arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public void addList(ArrayList<CartBean> arrayList) {
        this.arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }


    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater from = LayoutInflater.from(context);
        RecyclerView.ViewHolder holder = new CartViewHolder(from.inflate(R.layout.cart_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        cartViewHolder = (CartViewHolder) holder;
        final CartBean cartBean = arrayList.get(position);
        GoodDetailsBean goods = cartBean.getGoods();
        cartViewHolder.mtv_count.setText("" + cartBean.getCount());
        cartViewHolder.mcb_check.setChecked(cartBean.isChecked());
        if (goods != null) {
            cartViewHolder.mtv_name.setText(goods.getGoodsName());
            cartViewHolder.mtv_price.setText(goods.getRankPrice());
            ImageUtils.setNewGoodthumb(goods.getGoodsThumb(), cartViewHolder.mni_photo);
        }
        AddDeleteCarListener listener = new AddDeleteCarListener(goods);
        cartViewHolder.miv_add.setOnClickListener(listener);
        cartViewHolder.miv_delete.setOnClickListener(listener);
        cartViewHolder.mcb_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cartBean.setChecked(isChecked);
                new DownloadCartTask(context, cartBean).execute();
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList == null ? 0 : arrayList.size();
    }


    class CartViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView mni_photo;
        TextView mtv_name, mtv_price, mtv_count;
        ImageView miv_add, miv_delete;
        CheckBox mcb_check;

        public CartViewHolder(View itemView) {
            super(itemView);
            mni_photo = (NetworkImageView) itemView.findViewById(R.id.niv_cart);
            mtv_name = (TextView) itemView.findViewById(R.id.tv_name_cart);
            mtv_price = (TextView) itemView.findViewById(R.id.price_cart);
            mtv_count = (TextView) itemView.findViewById(R.id.count_cart);
            miv_add = (ImageView) itemView.findViewById(R.id.add_cart);
            miv_delete = (ImageView) itemView.findViewById(R.id.delet_cart);
            mcb_check = (CheckBox) itemView.findViewById(R.id.checked);
        }
    }

    class AddDeleteCarListener implements View.OnClickListener {

        GoodDetailsBean good;

        public AddDeleteCarListener(GoodDetailsBean good) {
            this.good = good;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add_cart:
                    Utils.addCart(context, good);
                    break;
                case R.id.delet_cart:
                    Utils.deleteCart(context,good);
                    break;
            }
        }
    }
}
