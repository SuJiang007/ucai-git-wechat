package cn.ucai.fulicenter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/6/15.
 */
public class FootViewHolder extends RecyclerView.ViewHolder{
    public TextView mtv_foot;
    public FootViewHolder(View itemView) {
        super(itemView);
        mtv_foot = (TextView) itemView.findViewById(R.id.foot);
    }
}
