package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FootViewHolder;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.Good_DetailActivity;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/18.
 */
public class Category_DetaiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;
    ArrayList<NewGoodBean> arrayList;
    static final int TYPE_ITEM=0;
    static final int TYPE_FOOTER=1;
    String footText;
    public boolean isMore;
    int sortBy;

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
        sort(sortBy);
        notifyDataSetChanged();
    }

    private void sort(final int sortBy) {
        Collections.sort(arrayList, new Comparator<NewGoodBean>() {
            @Override
            public int compare(NewGoodBean g1, NewGoodBean g2) {
                int result = 0;
                switch (sortBy) {
                    case I.SORT_BY_ADDTIME_ASC:
                        result = (int) (g1.getAddTime() - g2.getAddTime());
                        break;
                    case I.SORT_BY_ADDTIME_DESC:
                        result = (int) (g2.getAddTime() - g1.getAddTime());
                        break;
                    case I.SORT_BY_PRICE_ASC:
                    {
                        int p1 = convertPrice(g1.getCurrencyPrice());
                        int p2 = convertPrice(g2.getCurrencyPrice());
                        result = p1 - p2;
                    }
                    break;
                    case I.SORT_BY_PRICE_DESC:
                    {
                        int p1 = convertPrice(g1.getCurrencyPrice());
                        int p2 = convertPrice(g2.getCurrencyPrice());
                        result = p1 - p2;
                    }
                    break;
                }
                return result;
            }

            private int convertPrice(String price) {
                price = price.substring(price.indexOf("￥") + 1);
                int p1 = Integer.parseInt(price);
                return p1;
            }
        });
    }
    FootViewHolder footViewHolder;
    NewGoodsViewHolder newGoodsViewHolder;
    public Category_DetaiAdapter(Context context, ArrayList<NewGoodBean> arrayList,int sortBy) {
        this.sortBy = sortBy;
        this.context = context;
        this.arrayList = arrayList;
    }

    public void initList(ArrayList<NewGoodBean> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            this.arrayList.clear();
        }
        this.arrayList.addAll(arrayList);
        sort(sortBy);
        notifyDataSetChanged();
    }

    public void addList(ArrayList<NewGoodBean> arrayList) {
        this.arrayList.addAll(arrayList);
        sort(sortBy);
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
            final NewGoodBean newGoodBean = arrayList.get(position);
            newGoodsViewHolder.mtv_name.setText(newGoodBean.getGoodsName());
            newGoodsViewHolder.mtv_price.setText(newGoodBean.getCurrencyPrice());
            ImageUtils.setNewGoodthumb(newGoodBean.getGoodsThumb(), newGoodsViewHolder.mNetiv_photo);
            newGoodsViewHolder.mNetiv_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, Good_DetailActivity.class).putExtra(
                            D.NewGood.KEY_GOODS_ID,newGoodBean.getGoodsId()));
                }
            });
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
        LinearLayout mnew_good_detail;

        public NewGoodsViewHolder(View itemView) {
            super(itemView);
            mNetiv_photo = (NetworkImageView) itemView.findViewById(R.id.netiv_photo);
            mtv_name = (TextView) itemView.findViewById(R.id.good_tv_name);
            mtv_price = (TextView) itemView.findViewById(R.id.tv_price);
            mnew_good_detail = (LinearLayout) itemView.findViewById(R.id.new_good_detail);
        }
    }
}
