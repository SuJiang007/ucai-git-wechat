package cn.ucai.fulicenter.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/17.
 */
public class CategoryAdapter extends BaseExpandableListAdapter {
    Context context;
    ArrayList<CategoryGroupBean> GroupList;
    ArrayList<ArrayList<CategoryChildBean>> ChilidList;

    int position;
    public CategoryAdapter(Context context, ArrayList<CategoryGroupBean> groupList,
                           ArrayList<ArrayList<CategoryChildBean>> chilidList) {
        this.context = context;
        GroupList = groupList;
        ChilidList = chilidList;
    }

    @Override
    public int getGroupCount() {
        return GroupList == null? 0 : GroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ChilidList == null || ChilidList.get(groupPosition) == null ? 0 : ChilidList.get(groupPosition).size();
    }

    @Override
    public CategoryGroupBean getGroup(int groupPosition) {
        return GroupList.get(groupPosition);
    }

    @Override
    public CategoryChildBean getChild(int groupPosition, int childPosition) {
        return ChilidList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        CategoryGroupViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.category_group, null);
            holder = new CategoryGroupViewHolder();
            holder.category_group_photo = (NetworkImageView) convertView.findViewById(R.id.category_photo);
            holder.category_group_name = (TextView) convertView.findViewById(R.id.category_name);
            holder.category_group_Expand = (ImageView) convertView.findViewById(R.id.category_Expand);
            convertView.setTag(holder);
        } else {
            holder = (CategoryGroupViewHolder) convertView.getTag();
        }
        CategoryGroupBean group = getGroup(groupPosition);
        holder.category_group_name.setText(group.getName());
        String imageUrl = group.getImageUrl();
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_GROUP_IMAGE_URL + imageUrl;
        ImageUtils.setThumb(url,holder.category_group_photo);
        if (isExpanded) {
            holder.category_group_Expand.setImageResource(R.drawable.expand_off);
        } else {
            holder.category_group_Expand.setImageResource(R.drawable.expand_on);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        CategoryChildViewHolder holder = null;
        position = childPosition;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.category_child, null);
            holder = new CategoryChildViewHolder();
            holder.category_child_rl = (RelativeLayout) convertView.findViewById(R.id.category_child_rl);
            holder.category_child_phtot = (NetworkImageView) convertView.findViewById(R.id.category_child_phtot);
            holder.category_child_name = (TextView) convertView.findViewById(R.id.category_child_name);
            convertView.setTag(holder);
        } else {
            holder = (CategoryChildViewHolder) convertView.getTag();
        }
        CategoryChildBean child = getChild(groupPosition, childPosition);
        holder.category_child_name.setText(child.getName());
        String imageUrl = child.getImageUrl();
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_CHILD_IMAGE_URL + imageUrl;
        ImageUtils.setThumb(url,holder.category_child_phtot);
        holder.category_child_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void addGroupList(ArrayList<CategoryGroupBean> arrayList) {
        this.GroupList.addAll(arrayList);
        notifyDataSetChanged();
    }
    public void addChildList(ArrayList<CategoryChildBean> arrayList,int groupposition) {
        this.ChilidList.get(groupposition).addAll(arrayList);
        notifyDataSetChanged();
    }

    class CategoryGroupViewHolder {
        NetworkImageView category_group_photo;
        TextView category_group_name;
        ImageView category_group_Expand;
    }

    class CategoryChildViewHolder {
        RelativeLayout category_child_rl;
        NetworkImageView category_child_phtot;
        TextView category_child_name;
    }
}
