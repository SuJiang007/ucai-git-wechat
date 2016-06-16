package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.FootViewHolder;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/15.
 */
public class NewGoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;
    ArrayList<NewGoodBean> arrayList;
    static final int TYPE_ITEM=0;
    static final int TYPE_FOOTER=1;
    String footText;
    public boolean isMore;

    FootViewHolder footViewHolder;
    NewGoodsViewHolder newGoodsViewHolder;
    public NewGoodAdapter(Context context, ArrayList<NewGoodBean> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public void initList(ArrayList<NewGoodBean> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            this.arrayList.clear();
        }
        this.arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public void addList(ArrayList<NewGoodBean> arrayList) {
        this.arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public void setFootText(String footText) {
        this.footText = footText;
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
        RecyclerView.ViewHolder holder = null;
        LayoutInflater from = LayoutInflater.from(context);
        View layout;
        switch (viewType) {
            case TYPE_FOOTER:
                layout = from.inflate(R.layout.item_foot_table, parent, false);
                holder = new FootViewHolder(layout);
                break;
            case TYPE_ITEM:
                layout = from.inflate(R.layout.new_good, parent, false);
                holder = new NewGoodsViewHolder(layout);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FootViewHolder) {
            footViewHolder = (FootViewHolder) holder;
            footViewHolder.mtv_foot.setText(footText);
            footViewHolder.mtv_foot.setVisibility(View.VISIBLE);
        }
        if (holder instanceof NewGoodsViewHolder) {
            newGoodsViewHolder = (NewGoodsViewHolder) holder;
            NewGoodBean newGoodBean = arrayList.get(position);
            newGoodsViewHolder.mtv_name.setText(newGoodBean.getGoodsName());
            newGoodsViewHolder.mtv_price.setText(newGoodBean.getCurrencyPrice());
            newGoodsViewHolder.mNetiv_photo.setDefaultImageResId(R.drawable.default_image);
            ImageUtils.setNewGoodthumb(newGoodBean.getGoodsThumb(), newGoodsViewHolder.mNetiv_photo);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList==null?1:arrayList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    class NewGoodsViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView mNetiv_photo;
        TextView mtv_name,mtv_price;
        public NewGoodsViewHolder(View itemView) {
            super(itemView);
            mNetiv_photo = (NetworkImageView) itemView.findViewById(R.id.netiv_photo);
            mtv_name = (TextView) itemView.findViewById(R.id.good_tv_name);
            mtv_price = (TextView) itemView.findViewById(R.id.tv_price);
        }
    }

}
