package cn.ucai.fulicenter.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.FootViewHolder;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.Boutique_DetailActivity;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/17.
 */
public class BoutiqueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    FuliCenterMainActivity context;
    ArrayList<BoutiqueBean> arrayList;
    static final int TYPE_ITEM=0;
    static final int TYPE_FOOTER=1;
    String footText;
    public boolean isMore;

    FootViewHolder footViewHolder;
    BoutiqueViewHolder boutiqueViewHolder;
    public BoutiqueAdapter(FuliCenterMainActivity context, ArrayList<BoutiqueBean> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public void initList(ArrayList<BoutiqueBean> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            this.arrayList.clear();
        }
        this.arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public void addList(ArrayList<BoutiqueBean> arrayList) {
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
                layout = from.inflate(R.layout.boutique, parent, false);
                holder = new BoutiqueViewHolder(layout);
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
        if (holder instanceof BoutiqueViewHolder) {
            boutiqueViewHolder = (BoutiqueViewHolder) holder;
            final BoutiqueBean boutiqueBean = arrayList.get(position);
            boutiqueViewHolder.mtv_name.setText(boutiqueBean.getName());
            boutiqueViewHolder.mtv_title.setText(boutiqueBean.getTitle());
            boutiqueViewHolder.mtv_desc.setText(boutiqueBean.getDescription());
            ImageUtils.setBoutique(boutiqueBean.getImageurl(), boutiqueViewHolder.mNetiv_photo);
            boutiqueViewHolder.mNetiv_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, Boutique_DetailActivity.class);
                    intent.putExtra("CatId", boutiqueBean.getId());
                    intent.putExtra("boutiquename", boutiqueBean.getName());
                    context.startActivity(intent);
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

    class BoutiqueViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView mNetiv_photo;
        TextView mtv_name,mtv_title,mtv_desc;

        public BoutiqueViewHolder(View itemView) {
            super(itemView);
            mNetiv_photo = (NetworkImageView) itemView.findViewById(R.id.botique_photo);
            mtv_name = (TextView) itemView.findViewById(R.id.name_boutique);
            mtv_title = (TextView) itemView.findViewById(R.id.title_boutique);
            mtv_desc = (TextView) itemView.findViewById(R.id.des_boutique);
        }
    }
}
