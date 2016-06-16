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
package cn.ucai.fulicenter.utils;

import android.content.Context;
import android.os.Environment;

import com.android.volley.toolbox.NetworkImageView;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;

import java.io.File;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.SuperWeChatApplication;
import cn.ucai.fulicenter.data.RequestManager;

public class ImageUtils {
//	public static String getThumbnailImagePath(String imagePath) {
//		String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
//		path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
//		EMLog.d("msg", "original image path:" + imagePath);
//		EMLog.d("msg", "thum image path:" + path);
//		return path;
//	}
	
	public static String getImagePath(String remoteUrl)
	{
		String imageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
		String path =PathUtil.getInstance().getImagePath()+"/"+ imageName;
        EMLog.d("msg", "image path:" + path);
        return path;
		
	}
	
	
	public static String getThumbnailImagePath(String thumbRemoteUrl) {
		String thumbImageName= thumbRemoteUrl.substring(thumbRemoteUrl.lastIndexOf("/") + 1, thumbRemoteUrl.length());
		String path =PathUtil.getInstance().getImagePath()+"/"+ "th"+thumbImageName;
        EMLog.d("msg", "thum image path:" + path);
        return path;
    }

	public static String getAvatarpath(Context context, String path) {
		File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File folder = new File(dir, path);
		if (!folder.exists()) {
			folder.mkdir();
		}
		return folder.getAbsolutePath();
	}

	public static void setNewGoodthumb(String goodsThumb, NetworkImageView mNetiv_photo) {
		String path = SuperWeChatApplication.SERVER_ROOT + "?" + "request=" + I.REQUEST_DOWNLOAD_NEW_GOOD
				+ "&" + I.FILE_NAME + "=" + goodsThumb;
		mNetiv_photo.setImageUrl(path, RequestManager.getImageLoader());
		mNetiv_photo.setDefaultImageResId(R.drawable.nopic);
		mNetiv_photo.setErrorImageResId(R.drawable.nopic);
	}
}
