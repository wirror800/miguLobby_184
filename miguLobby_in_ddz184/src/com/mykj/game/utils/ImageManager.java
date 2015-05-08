package com.mykj.game.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.http.client.HttpClient;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.login.utils.DensityConst;

/**
 * 下载图片管理类
 * 
 * @author Administrator
 * 
 */
public class ImageManager {


	private static Vector<String> cacheLoadingUrl = new Vector<String>();

	private static ImageManager manager;

	private ImageManager() {
	}

	public static ImageManager getInstance() {
		if (manager == null)
			manager = new ImageManager();
		return manager;
	}


	/**
	 * 下载图片，当正在下载时，不会重复下载 下载完毕后会全部进行设置图片
	 * 
	 * @param context
	 * @param iv
	 * @param photFileName
	 * @param defaultDrawableResId
	 */
	public void loadImageView(Context context, ImageView iv,
			String photoFileName, int defaultDrawableResId) {
		// 设置图片
		iv.setTag(photoFileName);
		if (!Util.isEmptyStr(photoFileName)) {
			if (photoFileName.endsWith(".png")
					|| photoFileName.endsWith(".jpg")) {
				int end = photoFileName.length() - 4;
				String photoName = photoFileName.substring(0, end);
				int drawableId = context.getResources().getIdentifier(
						photoName, "drawable", context.getPackageName());
				if (drawableId > 0) { // res有图片
					iv.setImageResource(drawableId);
				} else {
					String iconDir = Util.getIconDir();
					File file = new File(iconDir, photoFileName);
					if (file.exists()) {
						Bitmap bitmap = BitmapFactory
								.decodeFile(file.getPath());
						if (bitmap != null) {
							int width = bitmap.getWidth();
							int height = bitmap.getHeight();
							int disWidth = DensityConst.getWidthPixels();
							Bitmap scaleBitmap = Bitmap.createScaledBitmap(
									bitmap, width * disWidth / 800, height
											* disWidth / 800, true);
							iv.setImageBitmap(scaleBitmap);
						} else {
							file.delete();
							iv.setImageResource(defaultDrawableResId);
							startDownLoadBitmap(photoFileName, iv);
						}

					} else {
						iv.setImageResource(defaultDrawableResId);
						startDownLoadBitmap(photoFileName, iv);
					}
				}
			}

		}

	}

	private void loadBitmap(final String photoName) {
		if (cacheLoadingUrl.contains(photoName)) {
			// 正在下载
			return;
		}
		cacheLoadingUrl.add(photoName);
		String url = AppConfig.imgUrl + photoName;
		downloading(url, photoName, null);

	}

	/**
	 * 根据图片名获取图片的绝对sd卡缓存路劲
	 *  若ImageName为null 或者 ""  则返回 "",
	 *  若ImageName不为空， 没有找到sd卡缓存路劲，则进行下载图片.返回值为""
	 *                   若找到了sd卡缓存路劲，则返回之；
	 * @param imageName
	 * @return
	 */
	public String getDirByImageName(String imageName) {
		if (!Util.isEmptyStr(imageName)) {
			String iconDir = Util.getIconDir();
			File file = new File(iconDir, imageName);
			if (file.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
				if (bitmap != null) {
					return file.getAbsolutePath();
				}else{
					file.delete();
				}
			}
			//没有找到就进行下载图片
			loadBitmap(imageName);
		}
		return "";
	}

	/**
	 * 开始下载,如果正在下载则不会下载了
	 * 
	 * @param photoFileName
	 * @param iv
	 */
	private void startDownLoadBitmap(final String photoFileName, final ImageView iv) {

		// 进行下载
		String url = AppConfig.imgUrl + photoFileName;

		downloading(url, photoFileName, iv);
	}
	
	/**
	 * 根据文件路劲名查找assets中图片
	 * @param context
	 * @param photo   例如：icon/badge_01.png
	 * @return
	 */
	public static Bitmap getImageFromAssetsFile(Context context,String photoPath){
		if(Util.isEmptyStr(photoPath)){
			return null;
		}
		try {
			AssetManager assetManager = context.getAssets();
			InputStream is = assetManager.open(photoPath);
			Bitmap bm = BitmapFactory.decodeStream(is);
			if (bm != null) {
				return bm;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 下载函数
	 * 
	 * @param url
	 * @param photoFileName
	 * @param listener
	 */
	private void downloading(String url, String photoFileName,
			ImageView image) {
		new ImageAsyncTaskDownload(url, photoFileName, image).execute();
	}

	public interface ImageDownLoadListener {
		public void onFinish(Bitmap bm);

		public void onFailed();
	}
}
