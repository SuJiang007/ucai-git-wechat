package cn.ucai.fulicenter.data;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
	public BitmapLruCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected int sizeOf(String key, Bitmap bitmap) {
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	@Override
	public Bitmap getBitmap(String url) {
		Bitmap bitmap = get(url);
		//如果没有在内存中找到则去磁盘缓存查找
		if(bitmap==null){
			bitmap = RequestManager.getBitmap(url);
			//如果磁盘缓存找到，添加到内存缓存中
			if(bitmap!=null){
				putBitmap(url,bitmap);
			}
		}
		return bitmap;
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		put(url, bitmap);
		//将图片同时添加到磁盘缓存中
		RequestManager.putBitmap(url,bitmap);
	}
}
