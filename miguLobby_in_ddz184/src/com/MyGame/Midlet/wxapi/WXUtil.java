package com.MyGame.Midlet.wxapi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.apache.http.conn.ConnectTimeoutException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class WXUtil {

	private static final String TAG = "SDK_Sample.Util";

	public static byte[] bmpToByteArray(final Bitmap bmp,
			final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static byte[] getHtmlByteArray(final String url) {
		URL htmlUrl = null;
		InputStream inStream = null;
		try {
			htmlUrl = new URL(url);
			URLConnection connection = htmlUrl.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				inStream = httpConnection.getInputStream();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] data = inputStreamToByte(inStream);

		return data;
	}

	public static byte[] inputStreamToByte(InputStream is) {
		try {
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] readFromFile(String fileName, int offset, int len) {
		if (fileName == null) {
			return null;
		}

		File file = new File(fileName);
		if (!file.exists()) {
			Log.i(TAG, "readFromFile: file not found");
			return null;
		}

		if (len == -1) {
			len = (int) file.length();
		}

		Log.d(TAG, "readFromFile : offset = " + offset + " len = " + len
				+ " offset + len = " + (offset + len));

		if (offset < 0) {
			Log.e(TAG, "readFromFile invalid offset:" + offset);
			return null;
		}
		if (len <= 0) {
			Log.e(TAG, "readFromFile invalid len:" + len);
			return null;
		}
		if (offset + len > (int) file.length()) {
			Log.e(TAG, "readFromFile invalid file len:" + file.length());
			return null;
		}

		byte[] b = null;
		try {
			RandomAccessFile in = new RandomAccessFile(fileName, "r");
			b = new byte[len]; // 创建合适文件大小的数组
			in.seek(offset);
			in.readFully(b);
			in.close();

		} catch (Exception e) {
			Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
			e.printStackTrace();
		}
		return b;
	}

	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

	public static Bitmap extractThumbNail(final String path, final int height,
			final int width, final boolean crop) {
		Assert.assertTrue(path != null && !path.equals("") && height > 0
				&& width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height
					+ ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = "
					+ beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY)
					: (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight
					+ ", orig=" + options.outWidth + "x" + options.outHeight
					+ ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				Log.e(TAG, "bitmap decode failed");
				return null;
			}

			Log.i(TAG,
					"bitmap decoded size=" + bm.getWidth() + "x"
							+ bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth,
					newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm,
						(bm.getWidth() - width) >> 1,
						(bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG,
						"bitmap croped size=" + bm.getWidth() + "x"
								+ bm.getHeight());
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}

		return null;
	}

	public static Bitmap extractThumbNail(Resources res, final int id,
			final int height, final int width, final boolean crop) {
		Assert.assertTrue(id != 0 && height > 0 && width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeResource(res, id, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height
					+ ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = "
					+ beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY)
					: (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight
					+ ", orig=" + options.outWidth + "x" + options.outHeight
					+ ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeResource(res, id, options);
			if (bm == null) {
				Log.e(TAG, "bitmap decode failed");
				return null;
			}

			Log.i(TAG,
					"bitmap decoded size=" + bm.getWidth() + "x"
							+ bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth,
					newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm,
						(bm.getWidth() - width) >> 1,
						(bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG,
						"bitmap croped size=" + bm.getWidth() + "x"
								+ bm.getHeight());
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}

		return null;
	}

	public static Dialog showAlert(final Context context,
			OnClickListener mWxListener, OnClickListener mTimelineListener,
			OnClickListener mMsgListener, OnCancelListener cancelListener) {
		final Dialog dlg = new Dialog(context, R.style.MMTheme_DataSheet);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.my_alert_dialog_menu_layout, null);
		final int cFullFillWidth = 10000;
		layout.setMinimumWidth(cFullFillWidth);
		ImageButton mShareWx = (ImageButton) layout.findViewById(R.id.share_wx);
		ImageButton mShareWxTimeline = (ImageButton) layout
				.findViewById(R.id.share_wx_timeline);
		ImageButton mShareMsg = (ImageButton) layout
				.findViewById(R.id.share_msg);
		Button mCancel = (Button) layout.findViewById(R.id.cancel);
		mShareWx.setOnClickListener(mWxListener);
		mShareWxTimeline.setOnClickListener(mTimelineListener);
		mShareMsg.setOnClickListener(mMsgListener);

		// set a large value put it in bottom
		Window w = dlg.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.x = 0;
		final int cMakeBottom = -1000;
		lp.y = cMakeBottom;
		lp.gravity = Gravity.BOTTOM;
		dlg.onWindowAttributesChanged(lp);
		dlg.setCanceledOnTouchOutside(true);
		if (cancelListener != null) {
			dlg.setOnCancelListener(cancelListener);
		}
		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlg.dismiss();
			}
		});
		dlg.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				((Activity) context).finish();
			}
		});
		dlg.setContentView(layout);
		dlg.show();
		return dlg;
	}

	private static final int MAX_TEST_COUNT = 3;

	public static class PopularizeInfo {
		public String title;
		public String Disc;
		public String toString(){
			return "WX PopularizeInfo title: " + title + ", Disc: " + Disc;
		}
	}

	private static List<PopularizeInfo> popularizeInfoList = new ArrayList<WXUtil.PopularizeInfo>();
	private static String WXGameDownLoadUrl = "";
	private static String SPKeyFlag = "******";
	private static String imgUrl = "";
	private static Bitmap iconDrawable = null;
	public static String getWXUrl() {
		return WXGameDownLoadUrl;
	}
	private static String WXImgDir = Util.getSdcardPath() + "/" + AppConfig.DOWNLOAD_FOLDER + "/" + "wxImgTemp";

	public static void requestWXUrl() {
		File dirFile = new File(WXImgDir);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		if (WXGameDownLoadUrl.length() == 0) {
			try {
				String result = null;
				for (int i = 0; i < MAX_TEST_COUNT; i++) {
					String reqUrl = getWXUrlReq(AppConfig.SHARE_DOWNPATH+"?");
					result = UtilHelper.doGetStatus(reqUrl);
					if (result != null && result.trim().length() > 0) {
						break;
					}

				}
				if (result == null || result.trim().length() == 0) {
					for (int i = 0; i < MAX_TEST_COUNT; i++) {
						String reqUrl = getWXUrlReq(AppConfig.SHARE_DOWNPATH1+"?");
						result = UtilHelper.doGetStatus(reqUrl);
						if (result != null
								&& result.trim().length() > 0) {
							break;
						}
					}
				}
				popularizeInfoList.clear();

				Log.i(TAG, "WX url Req = " + result);
				if (result != null
						&& result.toString().trim().length() > 0) {
					parseWXDownLoadUrlXml(result);
				}
				if (WXGameDownLoadUrl.length() == 0) {
					WXGameDownLoadUrl = AppConfig.NEW_HOST;
				}

				Log.i(TAG, "WX GAME URL = " + WXGameDownLoadUrl);

				if (popularizeInfoList.size() == 0) {
					PopularizeInfo info = new PopularizeInfo();
					info.title = AppConfig.mContext.getResources().getString(R.string.lucky_ddz);
					info.Disc = AppConfig.mContext.getResources().getString(R.string.weixin_input_tuiguangma)+"(" + SPKeyFlag +")"+AppConfig.mContext.getResources().getString(R.string.weixin_input_tuiguangma_gift);
					popularizeInfoList.add(info);
				}
				if(AppConfig.debug){
					for(PopularizeInfo info : popularizeInfoList){
						Log.i(TAG, info.toString());
					}
				}

				initWXImg();
			} catch (ConnectTimeoutException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static void initWXImg(){
		Log.i(TAG, "WXImageUrl: " + imgUrl);
		if(imgUrl != null && imgUrl.length() > 0){
			
			String imgName = WXImgDir + "/" + Util.getFileNameFromUrl(imgUrl);
		
			File file = new File(imgName);
			boolean needDownload = true;
			if(file.exists() && file.isFile()){
				iconDrawable = BitmapFactory.decodeFile(imgName);
				if(iconDrawable != null && iconDrawable.getHeight() > 0 && iconDrawable.getWidth() > 0){
					Log.i(TAG, "WXImg exist");
					needDownload = false;
				}else{
					if(iconDrawable != null){
						iconDrawable.recycle();
					}
					iconDrawable = null;
					file.delete();
					Log.i(TAG, "WXImg is a Wrong File, delete and dowload again");
				}
			}
			int downloadTime = 0;
			if(needDownload){
				do{
					if(Util.downloadResByHttp(imgUrl, imgName)) {
						Log.i(TAG, "WXImg downloaded");
						iconDrawable = BitmapFactory.decodeFile(imgName);
						if(iconDrawable != null && !(iconDrawable.getHeight() > 0 && iconDrawable.getWidth() > 0)){
							iconDrawable.recycle();
							iconDrawable = null;
							Log.i(TAG, "WXImg download a wrong file");
						}
						needDownload = false;
					}
					if(needDownload){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					downloadTime++;
				}while(needDownload && downloadTime < 2 && Util.isNetworkConnected(AppConfig.mContext));
			}
		}
	}
	
	public static Bitmap getWXIcon(){
		if(iconDrawable != null){
			Log.i(TAG, "WXImg is not default");
			return iconDrawable;
		}else{
			Log.i(TAG, "WXImg is default");
			return WXUtil.extractThumbNail(AppConfig.mContext.getResources(), R.drawable.mark_icon, WXEntryActivity.THUMB_SIZE,
					WXEntryActivity.THUMB_SIZE, true);
		}
	}
	
	public static String getWXUrlReq(String mMainUrl) {
		StringBuffer sb = new StringBuffer();

		sb.append(mMainUrl);
		sb.append("cmd=micromsg"); // 微信标识
		sb.append("&channelId=");
		sb.append(AppConfig.channelId);
		sb.append("&gameid=");
		sb.append(AppConfig.gameId);
		sb.append("&version=");
		sb.append(Util.getVersionName(AppConfig.mContext));
		sb.append("&env=");
		sb.append(AppConfig.getLaunchType());
		//sb.append("0");
		sb.append("&imsi=");
		sb.append(Util.getIMSI(AppConfig.mContext));
		Log.i(TAG, sb.toString());
		return sb.toString();
	}

	public static void parseWXDownLoadUrlXml(String strXml) {
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(strXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if (tagName.equals("package")) {
						WXGameDownLoadUrl = p.getAttributeValue(null, "Url");
					}else if(tagName.equals("item")){
						PopularizeInfo info = new PopularizeInfo();
						info.title = p.getAttributeValue(null, "title");
						info.Disc = p.getAttributeValue(null, "decr");
						popularizeInfoList.add(info);
					}else if(tagName.equals("boxs")){
						imgUrl = p.getAttributeValue(null, "img");
					}

					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (Exception e) {
			Log.v(TAG, "parse xml error");
		}
	}

	public static PopularizeInfo getPopularizeInfo(String spKey) {

		PopularizeInfo info = new PopularizeInfo();
		Random rand = new Random();
		int index = rand.nextInt(popularizeInfoList.size());
		info.title = popularizeInfoList.get(index).title;
		info.Disc = popularizeInfoList.get(index).Disc.replace(SPKeyFlag, spKey);
		return info;
	}
}
