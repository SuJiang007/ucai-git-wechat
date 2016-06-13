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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.easemob.EMCallBack;

import cn.ucai.git.I;
import cn.ucai.git.applib.controller.HXSDKHelper;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;

import cn.ucai.git.Constant;
import cn.ucai.git.SuperWeChatApplication;
import cn.ucai.git.DemoHXSDKHelper;
import cn.ucai.git.R;
import cn.ucai.git.bean.Avatar;
import cn.ucai.git.bean.User;
import cn.ucai.git.data.ApiParams;
import cn.ucai.git.data.GsonRequest;
import cn.ucai.git.data.OkHttpUtils;
import cn.ucai.git.db.EMUserDao;
import cn.ucai.git.db.UserDao;
import cn.ucai.git.domain.EMUser;
import cn.ucai.git.listener.OnSetAvatarListener;
import cn.ucai.git.task.DownloadAllGroupTask;
import cn.ucai.git.task.DownloadContactListTask;
import cn.ucai.git.task.DownloadImageTask;
import cn.ucai.git.task.DownloadPublicGroupTask;
import cn.ucai.git.utils.BitmapUtils;
import cn.ucai.git.utils.CommonUtils;
import cn.ucai.git.utils.ImageLoader;
import cn.ucai.git.utils.ImageUtils;
import cn.ucai.git.utils.MD5;
import cn.ucai.git.utils.Utils;

/**
 * 登陆页面
 *
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    public static final int REQUEST_CODE_SETNICK = 1;
    private EditText usernameEditText;
    private EditText passwordEditText;

    private boolean progressShow;
    private boolean autoLogin = false;

    private String currentUsername;
    private String currentPassword;
    ProgressDialog pd;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 如果用户名密码都有，直接进入主页面
        if (DemoHXSDKHelper.getInstance().isLogined()) {
            autoLogin = true;
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            return;
        }
        setContentView(R.layout.activity_login);
        mContext = this;
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);


        if (SuperWeChatApplication.getInstance().getUserName() != null) {
            usernameEditText.setText(SuperWeChatApplication.getInstance().getUserName());
        }
        setListener();
    }

    private void setListener() {
        setOnRegisterListener();
        setOnLoginListener();
        setOnUserNameChangedListener();
    }

    private void setOnUserNameChangedListener() {
        // 如果用户名改变，清空密码
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEditText.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 登录
     *
     */
    private void setOnLoginListener() {
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonUtils.isNetWorkConnected(LoginActivity.this)) {
                    Toast.makeText(mContext, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                currentUsername = usernameEditText.getText().toString().trim();
                currentPassword = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(currentUsername)) {
                    Toast.makeText(mContext, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(mContext, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog();
                final long start = System.currentTimeMillis();
                // 调用sdk登陆方法登陆聊天服务器
                EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        if (!progressShow) {
                            return;
                        }
                        loginAppServer();
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(final int code, final String message) {
                        if (!progressShow) {
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void loginAppServer() {
        UserDao dao = new UserDao(mContext);
        User user = dao.findUser(currentUsername);
        if (user != null) {
            if (user.getMUserPassword().equals(MD5.getData(currentPassword))) {
                loginSuccess();
            } else {
                pd.dismiss();
                Toast.makeText(mContext,R.string.login_failure_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                String path = new ApiParams()
                        .with(I.User.USER_NAME, currentUsername)
                        .with(I.User.PASSWORD, currentPassword)
                        .getRequestUrl(I.REQUEST_LOGIN);
                executeRequest(new GsonRequest<User>(path, User.class, responseListener(dao), errorListener()));
            } catch (Exception e) {
                e.printStackTrace();
                pd.dismiss();
            }
        }
    }

    private Response.Listener<User> responseListener(final UserDao dao) {
        return new Response.Listener<User>() {
                    @Override
                    public void onResponse(User user) {
                        if (user.isResult()) {
                            saveUser(user);
                            loginSuccess();
                            dao.addUser(user);
                        } else {
                            Utils.showToast(mContext, Utils.getResourceString(mContext, user.getMsg()), Toast.LENGTH_SHORT);
                            pd.dismiss();
                        }
                    }
                };
    }

    private void saveUser(User user) {
        SuperWeChatApplication instance = SuperWeChatApplication.getInstance();
        // 登陆成功，保存用户名密码
        instance.setUser(user);
        instance.setUserName(currentUsername);
        instance.setPassword(currentPassword);
        SuperWeChatApplication.currentUserNick = user.getMUserNick();
    }

    private void loginSuccess() {

        try {
            // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
            // ** manually load all local groups and
            EMGroupManager.getInstance().loadAllGroups();
            EMChatManager.getInstance().loadAllConversations();
            // 处理好友和群组
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new DownloadAllGroupTask(currentUsername, mContext).execute();
                    new DownloadContactListTask(currentUsername, mContext).execute();
                    new DownloadPublicGroupTask(I.PAGE_ID_DEFAULT, I.PAGE_SIZE_DEFAULT, currentUsername, mContext).execute();
                }
            });
            initializeContacts();
            //下载用户头像
            save2SD();
        } catch (Exception e) {
            e.printStackTrace();
            // 取好友或者群聊失败，不让进入主页面
            runOnUiThread(new Runnable() {
                public void run() {
                    pd.dismiss();
                    DemoHXSDKHelper.getInstance().logout(true, null);
                    Toast.makeText(getApplicationContext(), R.string.login_failure_failed, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
        boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
                SuperWeChatApplication.currentUserNick.trim());
        if (!updatenick) {
            Log.e("LoginActivity", "update current user nick fail");
        }
        if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
            pd.dismiss();
        }
        if (pd != null) {
            pd.dismiss();
        }
        // 进入主页面
        Intent intent = new Intent(LoginActivity.this,
                MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void save2SD() {
        final OkHttpUtils<Message> utils = new OkHttpUtils<Message>();
        utils.url(SuperWeChatApplication.ROOT_SERVER)
                .addParam(I.KEY_REQUEST, I.REQUEST_DOWNLOAD_AVATAR)
                .addParam(I.AVATAR_TYPE, currentUsername)
                .doInBackground(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                        String avatarPath = I.AVATAR_TYPE_USER_PATH + I.BACKSLASH
                                + currentUsername + I.AVATAR_SUFFIX_JPG;
                        File file = OnSetAvatarListener.getAvatarFile(LoginActivity.this, avatarPath);
                        FileOutputStream out = null;
                        out = new FileOutputStream(file);
                        utils.downloadFile(response, file, false);
                    }
                }).execute(null);
    }

    private void showProgressDialog() {
        progressShow = true;
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                progressShow = false;
            }
        });
        pd.setMessage(getString(R.string.Is_landing));
        pd.show();
    }

    private void initializeContacts() {
        Map<String, EMUser> userlist = new HashMap<String, EMUser>();
        // 添加user"申请与通知"
        EMUser newFriends = new EMUser();
        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
        String strChat = getResources().getString(
                R.string.Application_and_notify);
        newFriends.setNick(strChat);

        userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
        // 添加"群聊"
        EMUser groupUser = new EMUser();
        String strGroup = getResources().getString(R.string.group_chat);
        groupUser.setUsername(Constant.GROUP_USERNAME);
        groupUser.setNick(strGroup);
        groupUser.setHeader("");
        userlist.put(Constant.GROUP_USERNAME, groupUser);

        // 存入内存
        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
        // 存入db
        EMUserDao dao = new EMUserDao(LoginActivity.this);
        List<EMUser> users = new ArrayList<EMUser>(userlist.values());
        dao.saveContactList(users);
    }

    /**
     * 注册
     *
     */
    public void setOnRegisterListener() {
        findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), 0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoLogin) {
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pd != null) {
            pd.dismiss();
        }
    }
}
