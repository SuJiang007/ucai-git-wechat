/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import cn.ucai.git.I;
import cn.ucai.git.R;
import cn.ucai.git.SuperWeChatApplication;
import cn.ucai.git.bean.Message;
import cn.ucai.git.data.OkHttpUtils;
import cn.ucai.git.listener.OnSetAvatarListener;
import cn.ucai.git.utils.ImageUtils;
import cn.ucai.git.utils.Utils;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity {
	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;
	private EditText nickEditText;
	private ImageView iv_avatar;

	OnSetAvatarListener monSetAvatarListener;
	String mAvatar;
	String username;
	String pwd;
	String nick;
	String confirm_pwd;
	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initView();
		setListener();
	}

	private void setListener() {
		setOnRegister();
		setOnLogin();
		setOnAvatarListener();
	}

	private void setOnAvatarListener() {
        findViewById(R.id.layout_Photo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				monSetAvatarListener = new OnSetAvatarListener(RegisterActivity.this, R.id.layout_register, getAvatarName(), I.AVATAR_TYPE_USER_PATH);
			}
		});
	}

	private String getAvatarName() {
		mAvatar = System.currentTimeMillis() + "";
		return mAvatar;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			monSetAvatarListener.setAvatar(requestCode,data,iv_avatar);
		}
	}

	private void setOnLogin() {
		findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void initView() {
		userNameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		confirmPwdEditText = (EditText) findViewById(R.id.confirm_password);
		nickEditText = (EditText) findViewById(R.id.nick);
		iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
	}

	/**
	 * 注册
	 *
	 */
	public void setOnRegister() {
		findViewById(R.id.regist).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				username = userNameEditText.getText().toString().trim();
				pwd = passwordEditText.getText().toString().trim();
				nick = nickEditText.getText().toString().trim();
				confirm_pwd = confirmPwdEditText.getText().toString().trim();
				if (TextUtils.isEmpty(username)) {
					userNameEditText.requestFocus();
					userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_empty));
					return;
				} else if (!username.matches("[\\w][\\w\\d_]+")) {
					userNameEditText.requestFocus();
					userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_wd));
					return;
				} else if (TextUtils.isEmpty(nick)) {
					nickEditText.requestFocus();
					nickEditText.setError(getResources().getString(R.string.Nick_name_cannot_be_empty));
					return;
				} else if (TextUtils.isEmpty(pwd)) {
					passwordEditText.requestFocus();
					passwordEditText.setError(getResources().getString(R.string.Password_cannot_be_empty));
					return;
				} else if (TextUtils.isEmpty(confirm_pwd)) {
					confirmPwdEditText.requestFocus();
					confirmPwdEditText.setError(getResources().getString(R.string.Confirm_password_cannot_be_empty));
					return;
				} else if (!pwd.equals(confirm_pwd)) {
					Toast.makeText(RegisterActivity.this, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
					return;
				}

				if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
					pd = new ProgressDialog(RegisterActivity.this);
					pd.setMessage(getResources().getString(R.string.Is_the_registered));
					pd.show();
					registerAPPServer();
				}
			}
		});
	}

	private void registerAPPServer() {
		//先注册环信服务器
		//注册本地的服务器并上传头像
		//如果环信服务器注册失败，删除服务器上面的账号和头像
		File file = new File(ImageUtils.getAvatarpath(RegisterActivity.this, I.AVATAR_TYPE_USER_PATH), mAvatar + I.AVATAR_SUFFIX_JPG);
		final OkHttpUtils<Message> utils = new OkHttpUtils<>();
		utils.url(SuperWeChatApplication.ROOT_SERVER)
				.addParam(I.KEY_REQUEST,I.REQUEST_REGISTER)
				.addParam(I.User.USER_NAME,username)
				.addParam(I.User.NICK,nick)
				.addParam(I.User.PASSWORD,pwd)
				.targetClass(Message.class)
				.addFile(file)
				.execute(new OkHttpUtils.OnCompleteListener<Message>() {
					@Override
					public void onSuccess(Message result) {
						if (result.isResult()) {
							registerEMserver();
						} else {
							pd.dismiss();
							Utils.showToast(RegisterActivity.this,Utils.getResourceString(RegisterActivity.this,result.getMsg()),Toast.LENGTH_SHORT);
						}
					}

					@Override
					public void onError(String error) {
						pd.dismiss();
						Utils.showToast(RegisterActivity.this,error,Toast.LENGTH_SHORT);
					}
				});
	}


	private void registerEMserver() {
		new Thread(new Runnable() {
			public void run() {
				try {
					// 调用sdk注册方法
					EMChatManager.getInstance().createAccountOnServer(username, pwd);
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							// 保存用户名
							SuperWeChatApplication.getInstance().setUserName(username);
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
							finish();
						}
					});
				} catch (final EaseMobException e) {
					////10.0.2.2:8080/SuperWeChatServer/Server?request=unregister&m_user_name=
					OkHttpUtils<Message> utils = new OkHttpUtils<Message>();
					utils.url(SuperWeChatApplication.ROOT_SERVER)
							.addParam(I.KEY_REQUEST,I.REQUEST_UNREGISTER)
							.addParam(I.User.USER_NAME,username)
							.targetClass(Message.class)
							.execute(new OkHttpUtils.OnCompleteListener<Message>() {
								@Override
								public void onSuccess(Message result) {
									Utils.showToast(RegisterActivity.this,Utils.getResourceString(RegisterActivity.this,result.getMsg()),Toast.LENGTH_SHORT);
								}

								@Override
								public void onError(String error) {

								}
							});

					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							int errorCode=e.getErrorCode();
							if(errorCode==EMError.NONETWORK_ERROR){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.USER_ALREADY_EXISTS){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.UNAUTHORIZED){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.ILLEGAL_USER_NAME){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}
		}).start();
	}

	public void back(View view) {
		finish();
	}

}
