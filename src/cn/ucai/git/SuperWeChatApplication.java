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
package cn.ucai.git;

import android.app.Application;
import android.content.Context;

import com.easemob.EMCallBack;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.git.bean.Contact;
import cn.ucai.git.bean.Group;
import cn.ucai.git.bean.Member;
import cn.ucai.git.bean.User;

public class SuperWeChatApplication extends Application {

	public static Context applicationContext;
	private static SuperWeChatApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";

	public static String ROOT_SERVER = "http://10.0.2.2:9999/SuperWeChatServer/Server";
	
	/**
	 * 当前用户nickname,为了苹果推送不是userid而是昵称
	 */
	public static String currentUserNick = "";
	public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

	@Override
	public void onCreate() {
		super.onCreate();
        applicationContext = this;
        instance = this;

        /**
         * this function will initialize the HuanXin SDK
         * 
         * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
         * 
         * 环信初始化SDK帮助函数
         * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
         * 
         * for example:
         * 例子：
         * 
         * public class DemoHXSDKHelper extends HXSDKHelper
         * 
         * HXHelper = new DemoHXSDKHelper();
         * if(HXHelper.onInit(context)){
         *     // do HuanXin related work
         * }
         */
        hxSDKHelper.onInit(applicationContext);
	}

	public static SuperWeChatApplication getInstance() {
		return instance;
	}
 

	/**
	 * 获取当前登陆用户名
	 *
	 * @return
	 */
	public String getUserName() {
	    return hxSDKHelper.getHXId();
	}

	/**
	 * 获取密码
	 *
	 * @return
	 */
	public String getPassword() {
		return hxSDKHelper.getPassword();
	}

	/**
	 * 设置用户名
	 *
	 * @param username
	 */
	public void setUserName(String username) {
	    hxSDKHelper.setHXId(username);
	}

	/**
	 * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
	 * 内部的自动登录需要的密码，已经加密存储了
	 *
	 * @param pwd
	 */
	public void setPassword(String pwd) {
	    hxSDKHelper.setPassword(pwd);
	}

	/**
	 * 退出登录,清空数据
	 */
	public void logout(final boolean isGCM,final EMCallBack emCallBack) {
		// 先调用sdk logout，在清理app中自己的数据
	    hxSDKHelper.logout(isGCM,emCallBack);
	}

	/** 全局当前用户登录对象*/
	private User user;
	/** 全局当前用户好友对象*/
	private ArrayList<Contact> contactArrayList = new ArrayList<Contact>();
	/** 全局当前用户好友集合*/
	private HashMap<String, Contact> map = new HashMap<String, Contact>();
	/** 全局群组集合*/
	private ArrayList<Group> groupArrayList = new ArrayList<Group>();
	/** 全局群组公共的列表*/
	private ArrayList<Group> publicArrayList = new ArrayList<Group>();
	/** 全局群组成员的列表*/
	private HashMap<String, ArrayList<Member>> groupmember = new HashMap<String, ArrayList<Member>>();

	public HashMap<String, ArrayList<Member>> getGroupmember() {
		return groupmember;
	}

	public void setGroupmember(HashMap<String, ArrayList<Member>> groupmember) {
		this.groupmember = groupmember;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ArrayList<Contact> getContactArrayList() {
		return contactArrayList;
	}

	public void setContactArrayList(ArrayList<Contact> contactArrayList) {
		this.contactArrayList = contactArrayList;
	}

	public HashMap<String, Contact> getMap() {
		return map;
	}

	public void setMap(HashMap<String, Contact> map) {
		this.map = map;
	}

	public ArrayList<Group> getGroupArrayList() {
		return groupArrayList;
	}

	public void setGroupArrayList(ArrayList<Group> groupArrayList) {
		this.groupArrayList = groupArrayList;
	}

	public ArrayList<Group> getPublicArrayList() {
		return publicArrayList;
	}

	public void setPublicArrayList(ArrayList<Group> publicArrayList) {
		this.publicArrayList = publicArrayList;
	}
}
