package cn.ucai.fulicenter.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FootViewHolder;
import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CollectActivity;
import cn.ucai.fulicenter.activity.Good_DetailActivity;
import cn.ucai.fulicenter.bean.CollectBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/21.
 */
public class CollectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    CollectActivity context;
    ArrayList<CollectBean> arrayList;
    static final int TYPE_ITEM=0;
    static final int TYPE_FOOTER=1;
    String footText;
    public boolean isMore;
    FootViewHolder footViewHolder;
    CollectViewHolder collectViewHolder;
    public CollectAdapter(CollectActivity context, ArrayList<CollectBean> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public void initList(ArrayList<CollectBean> arrayList) {
        if (arrayList != null && !arrayList.isEmpty()) {
            this.arrayList.clear();
        }
        this.arrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public void addList(ArrayList<CollectBean> arrayList) {
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
                layout = from.inflate(R.layout.collect_item, parent, false);
                holder = new CollectViewHolder(layout);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FootViewHolder) {
            footViewHolder = (FootViewHolder) holder;
            footViewHolder.mtv_foot.setText(footText);
            footViewHolder.mtv_foot.setVisibility(View.VISIBLE);
        }
        if (holder instanceof CollectViewHolder) {
            collectViewHolder = (CollectViewHolder) holder;
            final CollectBean collectBean = arrayList.get(position);
            collectViewHolder.mtv_name.setText(collectBean.getGoodsName());
            collectViewHolder.mNetiv_photo.setDefaultImageResId(R.drawable.default_image);
            ImageUtils.setNewGoodthumb(collectBean.getGoodsThumb(), collectViewHolder.mNetiv_photo);
            collectViewHolder.miv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String path = new ApiParams()
                                .with(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                                .with(I.Collect.GOODS_ID, collectBean.getGoodsId() + "")
                                .getRequestUrl(I.REQUEST_DELETE_COLLECT);
                        Log.i("main", "path=" + path);
                        context.executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                                responseDeleteCollectListener(), context.errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                private Response.Listener<MessageBean> responseDeleteCollectListener() {
                    return new Response.Listener<MessageBean>() {
                        @Override
                        public void onResponse(MessageBean messageBean) {
                            if (messageBean.isSuccess()) {
                                new DownloadCollectCountTask(context).execute();
                                arrayList.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, messageBean.getMsg(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, messageBean.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                }
            });
            collectViewHolder.mnew_good_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, Good_DetailActivity.class);
                    intent.putExtra(D.NewGood.KEY_GOODS_ID, collectBean.getGoodsId());
                    context.startActivity(intent);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return arrayList==null?1:arrayList.size()+1;
    }

    public void deletecollect(ArrayList<CollectBean> arrayList) {
        arrayList.remove(arrayList);
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    class CollectViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView mNetiv_photo;
        TextView mtv_name;
        RelativeLayout mnew_good_detail;
        ImageView miv_delete;

        public CollectViewHolder(View itemView) {
            super(itemView);
            mNetiv_photo = (NetworkImageView) itemView.findViewById(R.id.netiv_photo_collect);
            mtv_name = (TextView) itemView.findViewById(R.id.good_tv_name_collect);
            mnew_good_detail = (RelativeLayout) itemView.findViewById(R.id.detai_collect);
            miv_delete = (ImageView) itemView.findViewById(R.id.delete_collect);
        }
    }

}
