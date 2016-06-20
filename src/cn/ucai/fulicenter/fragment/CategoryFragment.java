package cn.ucai.fulicenter.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.adapter.CategoryAdapter;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment {

    ArrayList<CategoryGroupBean> GroupList;
    ArrayList<ArrayList<CategoryChildBean>> ChildList;
    CategoryAdapter mAdapter;
    ExpandableListView mExpandableListView;
    FuliCenterMainActivity mContext;
    View layout;
    ArrayList<CategoryGroupBean> arrayList;
    int groupCount = 0;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = (FuliCenterMainActivity) getActivity();
        layout = inflater.inflate(R.layout.fragment_category, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        String path = "http://10.0.2.2:9999/FuLiCenterServer/Server?request=find_category_group";
        mContext.executeRequest(new GsonRequest<CategoryGroupBean[]>(path, CategoryGroupBean[].class,
                responseCategoryGroupListener(), mContext.errorListener()));
    }

    private Response.Listener<CategoryGroupBean[]> responseCategoryGroupListener() {
        return new Response.Listener<CategoryGroupBean[]>() {
            @Override
            public void onResponse(CategoryGroupBean[] categoryGroupBeen) {
                if (categoryGroupBeen != null) {
                    GroupList = Utils.array2List(categoryGroupBeen);
                    int i = 0;
                    for (CategoryGroupBean group : categoryGroupBeen) {
                        int id = categoryGroupBeen[i].getId();
                        ChildList.add(i,new ArrayList<CategoryChildBean>());
                        int page_id = 0;
                        try {
                            String url = new ApiParams()
                                    .with(I.CategoryChild.PARENT_ID, id + "")
                                    .with(I.PAGE_ID, page_id + "")
                                    .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                                    .getRequestUrl(I.REQUEST_FIND_CATEGORY_CHILDREN);
                            mContext.executeRequest(new GsonRequest<CategoryChildBean[]>(url, CategoryChildBean[].class,
                                    responseCategoryChildListener(i), mContext.errorListener()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        i++;
                    }

                }
            }
        };
    }

    private Response.Listener<CategoryChildBean[]> responseCategoryChildListener(final int i) {
        return new Response.Listener<CategoryChildBean[]>() {
            @Override
            public void onResponse(CategoryChildBean[] categoryChildBeen) {
                groupCount++;
                if (categoryChildBeen != null) {
                    ArrayList<CategoryChildBean> childlist
                            = Utils.array2List(categoryChildBeen);
                    if (childlist != null) {
                        ChildList.set(i, childlist);
                    }
                }
                if (GroupList.size() == groupCount) {
                    mAdapter.addListItem(GroupList,ChildList);
                }
            }
        };
    }


    private void initView() {
        mExpandableListView = (ExpandableListView) layout.findViewById(R.id.expandableListView);
        GroupList = new ArrayList<>();
        ChildList = new ArrayList<>();
        mAdapter = new CategoryAdapter(getContext(), GroupList, ChildList);
        mExpandableListView.setAdapter(mAdapter);
    }
}
