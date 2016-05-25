/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.git.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.ucai.git.I;
import cn.ucai.git.applib.controller.HXSDKHelper;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.easemob.chat.EMContactManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.ucai.git.SuperWeChatApplication;
import cn.ucai.git.DemoHXSDKHelper;
import cn.ucai.git.R;
import cn.ucai.git.bean.User;
import cn.ucai.git.data.ApiParams;
import cn.ucai.git.data.GsonRequest;
import cn.ucai.git.data.OkHttpUtils;
import cn.ucai.git.data.RequestManager;
import cn.ucai.git.listener.OnSetAvatarListener;
import cn.ucai.git.utils.UserUtils;

public class AddContactActivity extends BaseActivity {
    private EditText editText;
    private LinearLayout searchedUserLayout;
    private TextView nameText, mTextView, mresultText;
    private Button searchBtn;
    private NetworkImageView avatar;
    private InputMethodManager inputMethodManager;
    private String toAddUsername;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        mTextView = (TextView) findViewById(R.id.add_list_friends);

        mresultText = (TextView) findViewById(R.id.result);
        editText = (EditText) findViewById(R.id.edit_note);
        String strAdd = getResources().getString(R.string.add_friend);
        mTextView.setText(strAdd);
        String strUserName = getResources().getString(R.string.user_name);
        editText.setHint(strUserName);
        searchedUserLayout = (LinearLayout) findViewById(R.id.ll_user);
        nameText = (TextView) findViewById(R.id.name);
        searchBtn = (Button) findViewById(R.id.search);
        avatar = (NetworkImageView) findViewById(R.id.avatar);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }


    /**
     * 查找contact
     * @param v
     */
    public void searchContact(View v) {
        final String name = editText.getText().toString();
//        String saveText = searchBtn.getText().toString();
        if (TextUtils.isEmpty(name)) {
            String st = getResources().getString(R.string.Please_enter_a_username);
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", st));
            return;
        }
        toAddUsername = name;
        if (SuperWeChatApplication.getInstance().getUserName().equals(nameText.getText().toString())) {
            String str = getString(R.string.not_add_myself);
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", str));
            return;
        }

        // TODO 从服务器获取此contact,如果不存在提示不存在此用户

        try {
            String path = new ApiParams()
                    .with(I.User.USER_NAME, name)
                    .getRequestUrl(I.REQUEST_FIND_USER);
            executeRequest(new GsonRequest<User>(path,User.class,
                    responseFindUserListener(),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Response.Listener<User> responseFindUserListener() {
        return new Response.Listener<User>() {
            @Override
            public void onResponse(User user) {
                if (user != null) {
                    if (SuperWeChatApplication.getInstance().getMap().
                            containsKey(user.getMUserName())) {
                        startActivity(new Intent(AddContactActivity.this, UserProfileActivity.class).
                                putExtra("username", user.getMUserName()));
                    } else {
                        searchedUserLayout.setVisibility(View.VISIBLE);
                        String mnick = user.getMUserNick();
                        nameText.setText(mnick);
                        try {
                            String url = new ApiParams()
                                    .with(I.AVATAR_TYPE,toAddUsername)
                                    .getRequestUrl(I.REQUEST_DOWNLOAD_AVATAR);
                            avatar.setDefaultImageResId(R.drawable.default_image);
                            avatar.setErrorImageResId(R.drawable.default_image);
                            avatar.setImageUrl(url, RequestManager.getImageLoader());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mresultText.setVisibility(View.GONE);
                } else {
                    searchedUserLayout.setVisibility(View.GONE);
                    mresultText.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    /**
     *  添加contact
     * @param view
     */
    public void addContact(View view) {
        if (SuperWeChatApplication.getInstance().getUserName().equals(nameText.getText().toString())) {
            String str = getString(R.string.not_add_myself);
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", str));
            return;
        }

        if (((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().containsKey(nameText.getText().toString())) {
            //提示已在好友列表中，无需添加
            if (EMContactManager.getInstance().getBlackListUsernames().contains(nameText.getText().toString())) {
                startActivity(new Intent(this, AlertDialog.class).putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可"));
                return;
            }
            String strin = getString(R.string.This_user_is_already_your_friend);
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", strin));
            return;
        }

        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo写死了个reason，实际应该让用户手动填入
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMContactManager.getInstance().addContact(toAddUsername, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void back(View v) {
        finish();
    }
}
