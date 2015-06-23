package com.mykj.andr.logingift;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.mykj.andr.model.LotteryConfigInfo;
import com.mykj.andr.model.LotteryDrowPrize;
import com.mykj.andr.model.LotteryDrowWinner;
import com.mykj.andr.provider.LotteryDrowProvider;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

public class LotteryDrowHelper {

	private static final String TAG = "LOTTERY_DROW_HELPER";

	private static final int DESIGN_WIDTH_PX = 800;// 设计的宽度分辨率
	private static final int DESIGN_HEIGHT_PX = 480;// 设计的长度分辨率

	/**
	 * 根据设备分辨率获取需要计算的长宽比例值
	 * 
	 * @param dMetrics
	 *            设备相关参数
	 * @param pxValue
	 *            设计的像素值float[0]为宽度 float[1]为高度\
	 * @return 返回一个2维数组
	 */
	public static float[] getScale(Activity mActivity) {
		// 获取设备分辨率
		DisplayMetrics dMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
		int widthPX = dMetrics.widthPixels;
		int heightPX = dMetrics.heightPixels;
		// 计算缩放比例值
		float[] scaleValue = new float[2];
		scaleValue[0] = (float) widthPX / DESIGN_WIDTH_PX;
		scaleValue[1] = (float) heightPX / DESIGN_HEIGHT_PX;
		return scaleValue;
	}

	/**
	 * 根据路径获取assert目录下的图片
	 * 
	 * @param mContext
	 *            上下文
	 * @param imgPath
	 *            assert下图片路径
	 * @return
	 */
	public static Bitmap getBitmapFromAssert(Context mContext, String imgPath) {
		Bitmap bitmap = null;
		try {
			InputStream inputStream = mContext.getAssets().open(imgPath);
			bitmap = BitmapFactory.decodeStream(inputStream);
		} catch (IOException e) {
			return null;
		}
		return bitmap;
	}

	/**
	 * 根据图片路径，缩放成机器适配的bitmap
	 * 
	 * @param mContext上下文
	 * @param imgPath图片路径
	 * @param isAssertPath是否属于assert下的路径不是用全路径
	 * @return
	 */
	public static Bitmap sacleBitmapFromAssert(Context mContext,
			String imgPath, boolean isAssertPath, ArrayList<Bitmap> bitmapList) {
		Bitmap bitmap = null;
		bitmap = readBitmap(mContext, imgPath, Config.ARGB_4444, isAssertPath);
		if (bitmap == null) {
			Log.e(TAG, "缩放后的图片未找到,路径:" + imgPath);
			return null;
		}
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		if (LotteryDrowActivity.widthScale > 0
				&& LotteryDrowActivity.heightScale > 0 && bitmapWidth > 0
				&& bitmapHeight > 0) {
			// Matrix matrix = new Matrix();
			// matrix.postScale(LotteryDrowActivity.widthScale,
			// LotteryDrowActivity.heightScale);
			// Bitmap scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0,
			// bitmapWidth,
			// bitmapHeight, matrix, true);
			Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap,
					(int) (bitmapWidth * LotteryDrowActivity.widthScale),
					(int) (bitmapHeight * LotteryDrowActivity.heightScale),
					true);
			if (scaleBitmap.hashCode() != bitmap.hashCode()) {
				bitmap.recycle();
				bitmap = null;
			}
			if (bitmapList == null) {
				bitmapList = new ArrayList<Bitmap>();
			}
			if (scaleBitmap != null) {
				bitmapList.add(scaleBitmap);
			}
			return scaleBitmap;
		} else {
			return null;
		}
	}

	/***
	 * 获取资源的值
	 * 
	 * @param resourceID
	 *            资源id
	 * @param mContext
	 *            上下文
	 * @return
	 */
	public static float getDimension(int resourceID, Context mContext) {
		return mContext.getResources().getDimension(resourceID);
	}

	/**
	 * @param mContext
	 *            上下文
	 * @param layout
	 *            要放置的布局
	 * @param btn
	 *            要处理的按钮
	 * @param normalImg
	 *            没被按下时的背景
	 * @param pressImg
	 *            被按下时的背景
	 * @param leftMarginPos
	 *            离界面的位置
	 * @param topMarginPos
	 *            离界面上边的位置
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Button setBtnParam(Context mContext, RelativeLayout layout,
			final Button btn, Bitmap normalImg, Bitmap pressImg,
			int leftMarginPos, int topMarginPos) {
		final BitmapDrawable backBtnNormalDrawable = new BitmapDrawable(
				normalImg);
		final BitmapDrawable backBtnPressDrawable = new BitmapDrawable(pressImg);
		btn.setBackgroundDrawable(backBtnNormalDrawable);
		RelativeLayout.LayoutParams layoutParams = new LayoutParams(
				normalImg.getWidth(), normalImg.getHeight());
		layoutParams.setMargins(
				(int) (leftMarginPos * LotteryDrowActivity.widthScale),
				(int) (topMarginPos * LotteryDrowActivity.heightScale), 0, 0);
		btn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					btn.setBackgroundDrawable(backBtnPressDrawable);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					btn.setBackgroundDrawable(backBtnNormalDrawable);
				}
				return false;
			}
		});
		layout.addView(btn, layoutParams);
		return btn;
	}

	/**
	 * 设置组件的相关参数，包括位置以及大小等
	 */
	public static View setViewParam(View view, RelativeLayout layout,
			int leftMarginPos, int topMarginPos, int viewWidth, int viewHeight) {
		RelativeLayout.LayoutParams layoutParams = new LayoutParams(
				(int) (viewWidth * LotteryDrowActivity.widthScale + 0.5),
				(int) (viewHeight * LotteryDrowActivity.heightScale + 0.5));
		layoutParams.setMargins((int) (leftMarginPos
				* LotteryDrowActivity.widthScale + 0.5), (int) (topMarginPos
				* LotteryDrowActivity.heightScale + 0.5), 0, 0);
		layout.addView(view, layoutParams);
		return view;
	}

	/**
	 * 设置组件的相关参数(用于获取获奖列表)
	 */
	public static View setViewParam(View view, RelativeLayout layout,
			int leftMarginPos, int topMarginPos) {
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				(int) (RelativeLayout.LayoutParams.WRAP_CONTENT),
				(int) (RelativeLayout.LayoutParams.WRAP_CONTENT));
		layoutParams.setMargins((int) (leftMarginPos
				* LotteryDrowActivity.widthScale + 0.5), (int) (topMarginPos
				* LotteryDrowActivity.heightScale + 0.5), 0, 0);
		layout.addView(view, layoutParams);
		return view;
	}

	/**
	 * 设置抽奖机下载的父路径
	 * 
	 * @param fileParentPath
	 */
	public static String getLotteryParentPath(Context mContext) {
		String newFileParentPath = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		// sd卡存在用sd卡放置资源，否则用手机内存
		if (!sdCardExist) {
			newFileParentPath = mContext.getCacheDir().getAbsolutePath()
					+ AppConfig.LOTTERYBMP_PATH;// 获取手机内存根目录
		} else {
			newFileParentPath = Util.getSdcardPath()
					+ AppConfig.DOWNLOAD_FOLDER + AppConfig.LOTTERYBMP_PATH;
		}
		return newFileParentPath;
	}

	/**
	 * 下载抽奖机图片
	 * 
	 * @return 是否下载成功
	 */
	public static void downloadLotteryImgs(final Context mContext,
			final String parentFilePath,
			final ArrayList<LotteryDrowPrize> loDrowPrizes,
			final String localLotteryVersion,
			final LotteryConfigInfo lotteryConfig, final Handler mLotteryHandler) {
		if (mContext == null || loDrowPrizes == null || lotteryConfig == null
				|| localLotteryVersion == null) {
			return;
		}
		Thread mThread = new Thread() {
			public void run() {
				LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
				// sharePreference里查看是否已经下载完成了
				boolean isDownloadFinish = Util.getBooleanSharedPreferences(
						mContext,
						LotteryDrowProvider.BITMAPS_DOWNLOAD_FINISH_TAG, false);
				// 判断目录里是否存在文件，不存在需要下载抽奖图片
				boolean isFilesExist = true;
				for (int i = 0; i < loDrowPrizes.size(); i++) {
					String resPathName = parentFilePath + "/"
							+ loDrowPrizes.get(i).getFileName();
					File resFile = new File(resPathName);
					if (!resFile.exists()) {
						isFilesExist = false;
						break;
					}
				}
				boolean isNeedDownLoad = true;
				if (isFilesExist && isDownloadFinish) {
					isNeedDownLoad = false;
				}
				// 判断版本，相同版本的并且目录中有文件的可以跳过不用下载
				if (localLotteryVersion != null
						&& !localLotteryVersion.equals("")
						&& localLotteryVersion
								.equals(lotteryConfig.multiVersion)
						&& !isNeedDownLoad) {
					for (int i = 0; i < loDrowPrizes.size(); i++) {
						LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
						String fileName = loDrowPrize.getFileName();
						String pathName = Util.getLotteryDir() + "/" + fileName;
						// Bitmap mBitmap = BitmapFactory.decodeFile(pathName);
						Bitmap mBitmap = LotteryDrowHelper
								.sacleBitmapFromAssert(mContext, pathName,
										false, loDrowActivity.getBitmapList());
						loDrowPrize.setPrizeBitmap(mBitmap);
						loDrowPrize.setAnimShowBitmap(LotteryDrowHelper
								.buildShowPrizeBitmap(loDrowPrize,
										loDrowActivity.getBitmapList()));
					}
					mLotteryHandler
							.sendEmptyMessage(LotteryDrowActivity.HANDLER_MULTI_LOTTERY_DL_SUCCESS);

				} else {
					boolean isDLSuccess = downloadLotteryImgs(mContext,
							lotteryConfig, loDrowPrizes, parentFilePath,
							mLotteryHandler);
					if (isDLSuccess) {
						mLotteryHandler
								.sendEmptyMessage(LotteryDrowActivity.HANDLER_MULTI_LOTTERY_DL_SUCCESS);

					} else {
						mLotteryHandler
								.sendEmptyMessage(LotteryDrowActivity.HANDLER_MULTI_LOTTERY_DL_FAIL);
						Util.setBooleanSharedPreferences(
								mContext,
								LotteryDrowProvider.BITMAPS_DOWNLOAD_FINISH_TAG,
								false);
					}
				}
			};
		};
		mThread.start();
	}

	// 下载抽奖机图片
	// return 下载是否成功
	private static boolean downloadLotteryImgs(Context mContext,
			LotteryConfigInfo lotteryConfig,
			ArrayList<LotteryDrowPrize> loDrowPrizes, String parentFilePath,
			Handler mLotteryHandler) {
		if (loDrowPrizes == null || loDrowPrizes.size() <= 0) {
			return false;
		}
		LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
		String lurl = lotteryConfig.multiLurl;
		for (int i = 0; i < loDrowPrizes.size(); i++) {
			LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
			String fileName = loDrowPrize.getFileName();
			String pathName = Util.getLotteryDir() + "/" + fileName;
			String loFileName = parentFilePath + "/"
					+ loDrowPrizes.get(i).getFileName();
			final File file = new File(loFileName);
			final File tempFile = new File(loFileName + ".tmp");
			final String url = lurl + loDrowPrizes.get(i).getFileName();
			boolean isDLFinish = false;// 判断单个文件是否下载完成，没下载完成，循环下载
			int downloadNum = 0;
			while (isDLFinish == false) {
				if (isDLFinish == false) {
					if (file.exists()) {
						file.delete();
					}
					if (tempFile.exists()) {
						tempFile.delete();
					}
				}
				isDLFinish = Util.downloadResByHttp(url, loFileName);
				downloadNum++;
				if (downloadNum > LotteryDrowProvider.BITMAPS_DOWNLOAD_LOOP_NUM) {
					// Bitmap fBitmap =
					// loDrowActivity.getLotteryMainView().getLotteryDrowView()
					// .getDefaultPrizeImg();
					// loDrowPrize.setPrizeBitmap(fBitmap);
					// loDrowPrize.setAnimShowBitmap(LotteryDrowHelper
					// .buildShowPrizeBitmap(loDrowPrize,
					// loDrowActivity.getBitmapList()));
					// 下载失败
					return false;

				}
			}
			// 单个图片成功下载，发送更新图片消息
			Bitmap mBitmap = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					pathName, false, loDrowActivity.getBitmapList());
			loDrowPrize.setPrizeBitmap(mBitmap);
			loDrowPrize.setAnimShowBitmap(LotteryDrowHelper
					.buildShowPrizeBitmap(loDrowPrize,
							loDrowActivity.getBitmapList()));
			loDrowActivity.getLotteryMainView().getLotteryDrowView().postInvalidate();
			// Message msg = mLotteryHandler.obtainMessage();
			// msg.what = LotteryDrowActivity.HANDLER_MULTI_LOTTERY_DL_SUCCESS;
			// msg.arg1 = i;
			// msg.arg2 = LotteryDrowProvider.BITMAPS_DOWNLOAD_NO_FINISH;
			// mLotteryHandler.sendMessage(msg);

		}
		;
		return true;
	}

	/**
	 * @param loDrowPrizes
	 *            抽奖机奖品集合
	 * @param loPrizesXml需要解析的抽奖机奖品列表
	 */
	public static boolean parseLotteryPrizesXML(
			ArrayList<LotteryDrowPrize> loDrowPrizes, String loPrizesXml) {
		boolean isParseSuccess = false;
		if (Util.isEmptyStr(loPrizesXml)) {
			return isParseSuccess;
		}
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(loPrizesXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if (tagName.equals("b")) {
						LotteryDrowPrize loDrowPrize = new LotteryDrowPrize(
								Integer.parseInt(p.getAttributeValue(null, "i")));
						loDrowPrize.setType(Integer.parseInt(p
								.getAttributeValue(null, "t")));
						loDrowPrize.setId(Integer.parseInt(p.getAttributeValue(
								null, "p")));
						loDrowPrize.setNum(Integer.parseInt(p
								.getAttributeValue(null, "c")));
						loDrowPrize.setName(p.getAttributeValue(null, "d"));
						loDrowPrize.setFileName(p
								.getAttributeValue(null, "bit"));
						// 添加进提供器中
						loDrowPrizes.add(loDrowPrize);
					}
					isParseSuccess = true;
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
			Log.e(TAG, "抽奖机解析XML文件出错！XML文件格式有误,或者文件保存格式有误！");
			isParseSuccess = false;
		}
		return isParseSuccess;
	}

	/**
	 * @param loDrowPrizes
	 *            抽奖机奖品集合
	 * @param loPrizesXml需要解析的抽奖机奖品列表
	 */
	public static boolean parseMultiLotteryPrizesXML(
			ArrayList<LotteryDrowPrize> multiLoDrowPrizes,
			String loMultiPrizesXml) {
		boolean isParseSuccess = false;
		if (Util.isEmptyStr(loMultiPrizesXml)) {
			return isParseSuccess;
		}
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(loMultiPrizesXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if (tagName.equals("b")) {
						LotteryDrowPrize loDrowPrize = new LotteryDrowPrize(
								Integer.parseInt(p.getAttributeValue(null, "i")));
						loDrowPrize.setId(Integer.parseInt(p.getAttributeValue(
								null, "p")));
						loDrowPrize.setPrizeNum(Integer.parseInt(p
								.getAttributeValue(null, "c")));
						loDrowPrize.setName(p.getAttributeValue(null, "d"));
						// 添加进提供器中
						multiLoDrowPrizes.add(loDrowPrize);
					}
					isParseSuccess = true;
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
			Log.e(TAG, "抽奖机解析XML文件出错！XML文件格式有误,或者文件保存格式有误！");
			isParseSuccess = false;
		}
		return isParseSuccess;
	}

	/**
	 * @param loDrowPrizes
	 *            抽奖机奖品集合
	 * @param loPrizesXml需要解析的抽奖机奖品列表
	 */
	public static boolean parseLotteryWinnersXML(
			ArrayList<LotteryDrowWinner> loWinners, String loWinnerXml) {
		boolean isParseSuccess = false;
		if (Util.isEmptyStr(loWinnerXml)) {
			return isParseSuccess;
		}
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(loWinnerXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if (tagName.equals("b")) {
						LotteryDrowWinner loDrowWinner = new LotteryDrowWinner();
						loDrowWinner.setIndex(Integer.parseInt(p
								.getAttributeValue(null, "i")));
						loDrowWinner.setName(p.getAttributeValue(null, "n"));
						loDrowWinner.setNum(Integer.parseInt(p
								.getAttributeValue(null, "c")));
						loDrowWinner.setProId(Integer.parseInt(p
								.getAttributeValue(null, "p")));
						loDrowWinner.setProDes(p.getAttributeValue(null, "d"));
						// 添加进提供器中
						loWinners.add(loDrowWinner);
					}
					isParseSuccess = true;
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
			Log.e(TAG, "抽奖机解析XML文件出错！XML文件格式有误,或者文件保存格式有误！");
			isParseSuccess = false;
		}
		return isParseSuccess;
	}

	/**
	 * 获取乐豆的数字的图片
	 * 
	 * @param mContext
	 * @param num
	 * @param numBitmaps
	 * @return
	 */
	public static Bitmap getLedouBitmapFromNum(Context mContext, int num,
			HashMap<String, Bitmap> numBitmaps) {
		if (num < 0) {
			return null;
		}
		char[] nums = (num + "").toCharArray();
		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		for (int i = 0; i < nums.length; i++) {
			char tempNum = nums[i];
			switch (tempNum) {
			case '0':
				Bitmap bitmap = numBitmaps.get("0");
				if (bitmap != null) {
					bitmaps.add(bitmap);
				}
				break;
			case '1':
				Bitmap bitmap1 = numBitmaps.get("1");
				if (bitmap1 != null) {
					bitmaps.add(bitmap1);
				}
				break;
			case '2':
				Bitmap bitmap2 = numBitmaps.get("2");
				if (bitmap2 != null) {
					bitmaps.add(bitmap2);
				}
				break;
			case '3':
				Bitmap bitmap3 = numBitmaps.get("3");
				if (bitmap3 != null) {
					bitmaps.add(bitmap3);
				}
				break;
			case '4':
				Bitmap bitmap4 = numBitmaps.get("4");
				if (bitmap4 != null) {
					bitmaps.add(bitmap4);
				}
				break;
			case '5':
				Bitmap bitmap5 = numBitmaps.get("5");
				if (bitmap5 != null) {
					bitmaps.add(bitmap5);
				}
				break;
			case '6':
				Bitmap bitmap6 = numBitmaps.get("6");
				if (bitmap6 != null) {
					bitmaps.add(bitmap6);
				}
				break;
			case '7':
				Bitmap bitmap7 = numBitmaps.get("7");
				if (bitmap7 != null) {
					bitmaps.add(bitmap7);
				}
				break;
			case '8':
				Bitmap bitmap8 = numBitmaps.get("8");
				if (bitmap8 != null) {
					bitmaps.add(bitmap8);
				}
				break;
			case '9':
				Bitmap bitmap9 = numBitmaps.get("9");
				if (bitmap9 != null) {
					bitmaps.add(bitmap9);
				}
				break;
			}
		}
		return buildBitmap(bitmaps);
	}

	/**
	 * 抽奖机一键抽奖结束动画数量展示的图片
	 * 
	 * @param mContext
	 * @param num
	 *            展示的数字
	 * @param numBitmaps
	 *            每个数字对应的图片组
	 * @return
	 */
	public static Bitmap getAnimBitmapFromNum(Context mContext, int num,
			HashMap<String, Bitmap> numBitmaps) {
		if (num <= 0) {
			return null;
		}
		char[] nums = (num + "").toCharArray();
		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		bitmaps.add(numBitmaps.get("+"));
		for (int i = 0; i < nums.length; i++) {
			char tempNum = nums[i];
			switch (tempNum) {
			case '0':
				Bitmap bitmap = numBitmaps.get("0");
				if (bitmap != null) {
					bitmaps.add(bitmap);
				}
				break;
			case '1':
				Bitmap bitmap1 = numBitmaps.get("1");
				if (bitmap1 != null) {
					bitmaps.add(bitmap1);
				}
				break;
			case '2':
				Bitmap bitmap2 = numBitmaps.get("2");
				if (bitmap2 != null) {
					bitmaps.add(bitmap2);
				}
				break;
			case '3':
				Bitmap bitmap3 = numBitmaps.get("3");
				if (bitmap3 != null) {
					bitmaps.add(bitmap3);
				}
				break;
			case '4':
				Bitmap bitmap4 = numBitmaps.get("4");
				if (bitmap4 != null) {
					bitmaps.add(bitmap4);
				}
				break;
			case '5':
				Bitmap bitmap5 = numBitmaps.get("5");
				if (bitmap5 != null) {
					bitmaps.add(bitmap5);
				}
				break;
			case '6':
				Bitmap bitmap6 = numBitmaps.get("6");
				if (bitmap6 != null) {
					bitmaps.add(bitmap6);
				}
				break;
			case '7':
				Bitmap bitmap7 = numBitmaps.get("7");
				if (bitmap7 != null) {
					bitmaps.add(bitmap7);
				}
				break;
			case '8':
				Bitmap bitmap8 = numBitmaps.get("8");
				if (bitmap8 != null) {
					bitmaps.add(bitmap8);
				}
				break;
			case '9':
				Bitmap bitmap9 = numBitmaps.get("9");
				if (bitmap9 != null) {
					bitmaps.add(bitmap9);
				}
				break;
			}
		}
		return buildBitmap(bitmaps);
	}

	/**
	 * 抽奖机中根据数字组装bitmap
	 * 
	 * @return
	 */
	public static Bitmap getBitmapFromNum(Context mContext, int num,
			HashMap<String, Bitmap> numBitmaps) {
		Resources resource = mContext.getResources();
		String numStr = null;
		if (num >= 10000 && num < 100000000) {
			if (num % 10000 == 0) {
				numStr = num / 10000 + resource.getString(R.string.lucky_wan);
			} else {
				numStr = (float) num / 10000
						+ resource.getString(R.string.lucky_wan);
			}
		} else if (num >= 100000000) {
			if (num % 100000000 == 0) {
				numStr = num / 100000000
						+ resource.getString(R.string.lucky_yi);
			} else {
				numStr = (float) num / 100000000
						+ resource.getString(R.string.lucky_yi);
			}
		} else {
			numStr = num + "";
		}
		char[] nums = numStr.toCharArray();
		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		for (int i = 0; i < nums.length; i++) {
			char tempNum = nums[i];
			switch (tempNum) {
			case '0':
				Bitmap bitmap = numBitmaps.get("0");
				if (bitmap != null) {
					bitmaps.add(bitmap);
				}
				break;
			case '1':
				Bitmap bitmap1 = numBitmaps.get("1");
				if (bitmap1 != null) {
					bitmaps.add(bitmap1);
				}
				break;
			case '2':
				Bitmap bitmap2 = numBitmaps.get("2");
				if (bitmap2 != null) {
					bitmaps.add(bitmap2);
				}
				break;
			case '3':
				Bitmap bitmap3 = numBitmaps.get("3");
				if (bitmap3 != null) {
					bitmaps.add(bitmap3);
				}
				break;
			case '4':
				Bitmap bitmap4 = numBitmaps.get("4");
				if (bitmap4 != null) {
					bitmaps.add(bitmap4);
				}
				break;
			case '5':
				Bitmap bitmap5 = numBitmaps.get("5");
				if (bitmap5 != null) {
					bitmaps.add(bitmap5);
				}
				break;
			case '6':
				Bitmap bitmap6 = numBitmaps.get("6");
				if (bitmap6 != null) {
					bitmaps.add(bitmap6);
				}
				break;
			case '7':
				Bitmap bitmap7 = numBitmaps.get("7");
				if (bitmap7 != null) {
					bitmaps.add(bitmap7);
				}
				break;
			case '8':
				Bitmap bitmap8 = numBitmaps.get("8");
				if (bitmap8 != null) {
					bitmaps.add(bitmap8);
				}
				break;
			case '9':
				Bitmap bitmap9 = numBitmaps.get("9");
				if (bitmap9 != null) {
					bitmaps.add(bitmap9);
				}
				break;
			case '万':
				Bitmap bitmapWan = numBitmaps.get("万");
				if (bitmapWan != null) {
					bitmaps.add(bitmapWan);
				}
				break;
			case '亿':
				Bitmap bitmapYi = numBitmaps.get("亿");
				if (bitmapYi != null) {
					bitmaps.add(bitmapYi);
				}
				break;
			case '.':
				Bitmap bitmapDian = numBitmaps.get(".");
				if (bitmapDian != null) {
					bitmaps.add(bitmapDian);
				}
				break;
			default:
				break;
			}
		}
		return buildBitmap(bitmaps);
	}

	// 按图片集合按顺序组装图片
	private static Bitmap buildBitmap(ArrayList<Bitmap> bitmaps) {
		if (bitmaps == null || bitmaps.size() == 0) {
			return null;
		}
		int width = 0;
		int height = 0;
		for (int i = 0; i < bitmaps.size(); i++) {
			width = width + bitmaps.get(i).getWidth();
			height = Math.max(height, bitmaps.get(i).getHeight());
		}
		Bitmap resultBitmap = Bitmap.createBitmap(width, height,
				Config.ARGB_4444);
		int drawWidth = 0;
		Canvas canvas = new Canvas(resultBitmap);
		for (int j = 0; j < bitmaps.size(); j++) {
			drawWidth = j * bitmaps.get(j).getWidth();
			canvas.drawBitmap(bitmaps.get(j), drawWidth, 0, null);
		}
		return resultBitmap;
	}

	/**
	 * 
	 * @param bitmap
	 * @param xPiece横向切割片数
	 * @param yPiece纵向切割片数
	 * @return
	 */
	public static HashMap<String, Bitmap> splitNumAnimBitmap(Bitmap bitmap,
			int xPiece, int yPiece) {
		HashMap<String, Bitmap> pieces = new HashMap<String, Bitmap>();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float pieceWidth = (float) width / xPiece;
		float pieceHeight = (float) height / yPiece;
		for (int i = 0; i < yPiece; i++) {
			for (int j = 0; j < xPiece; j++) {
				int cellWidth = Math.round(pieceWidth);
				int cellHeight = Math.round(pieceHeight);
				int imgWidth = Math.round(j * pieceWidth);
				int imgHeight = Math.round(i * pieceHeight);
				Bitmap piece = Bitmap.createBitmap(bitmap, imgWidth, imgHeight,
						cellWidth, cellHeight);
				String bitmapTag = null;
				switch (j) {
				case 0:
					bitmapTag = "+";
					break;
				case 1:
					bitmapTag = 0 + "";
					break;
				case 2:
					bitmapTag = 1 + "";
					break;
				case 3:
					bitmapTag = 2 + "";
					break;
				case 4:
					bitmapTag = 3 + "";
					break;
				case 5:
					bitmapTag = 4 + "";
					break;
				case 6:
					bitmapTag = 5 + "";
					break;
				case 7:
					bitmapTag = 6 + "";
					break;
				case 8:
					bitmapTag = 7 + "";
					break;
				case 9:
					bitmapTag = 8 + "";
					break;
				case 10:
					bitmapTag = 9 + "";
					break;
				default:
					break;
				}
				pieces.put(bitmapTag, piece);
			}
		}
		return pieces;
	}

	/**
	 * 
	 * @param bitmap
	 * @param xPiece横向切割片数
	 * @param yPiece纵向切割片数
	 * @return
	 */
	public static HashMap<String, Bitmap> splitBitmap(Bitmap bitmap,
			int xPiece, int yPiece) {
		HashMap<String, Bitmap> pieces = new HashMap<String, Bitmap>();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float pieceWidth = (float) width / xPiece;
		float pieceHeight = (float) height / yPiece;
		for (int i = 0; i < yPiece; i++) {
			for (int j = 0; j < xPiece; j++) {
				int cellWidth = Math.round(pieceWidth);
				int cellHeight = Math.round(pieceHeight);
				int imgWidth = Math.round(j * pieceWidth);
				int imgHeight = Math.round(i * pieceHeight);
				Bitmap piece = Bitmap.createBitmap(bitmap, imgWidth, imgHeight,
						cellWidth, cellHeight);
				String bitmapTag = null;
				switch (j) {
				case 0:
					bitmapTag = 0 + "";
					break;
				case 1:
					bitmapTag = 1 + "";
					break;
				case 2:
					bitmapTag = 2 + "";
					break;
				case 3:
					bitmapTag = 3 + "";
					break;
				case 4:
					bitmapTag = 4 + "";
					break;
				case 5:
					bitmapTag = 5 + "";
					break;
				case 6:
					bitmapTag = 6 + "";
					break;
				case 7:
					bitmapTag = 7 + "";
					break;
				case 8:
					bitmapTag = 8 + "";
					break;
				case 9:
					bitmapTag = 9 + "";
					break;
				case 10:
					bitmapTag = "万";
					break;
				case 11:
					bitmapTag = "亿";
				case 12:
					bitmapTag = ".";
				default:
					break;
				}
				pieces.put(bitmapTag, piece);
			}
		}
		return pieces;
	}

	/**
	 * 组装抽奖按钮的图片
	 * 
	 * @return
	 */
	public static Bitmap buildMultiDrowBtnBitmap(Context mContext,
			int multiLoTimes, Bitmap btnBitmap) {
		// Bitmap multiTimesBMP =
		// LotteryDrowHelper.getBitmapFromAssert(mContext,
		// "lo/lo_times.png");
		LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
		Bitmap multiTimesBMP = LotteryDrowHelper.sacleBitmapFromAssert(
				mContext, "lo/lo_times.png", true,
				loDrowActivity.getBitmapList());
		Bitmap multiTimesBtnBmp = LotteryDrowHelper.getBitmapFromNum(mContext,
				multiLoTimes,
				LotteryDrowHelper.splitBitmap(multiTimesBMP, 10, 1));
		Canvas mCanvas = new Canvas(btnBitmap);
		Paint mPaint = new Paint();
		mCanvas.drawBitmap(multiTimesBtnBmp,
				120 * LotteryDrowActivity.widthScale,
				20 * LotteryDrowActivity.heightScale, mPaint);
		return btnBitmap;
	}

	/**
	 * 用于组装展示的奖品的图片
	 * 
	 * @param loDrowPrize
	 * @return
	 */
	public static Bitmap buildShowPrizeBitmap(LotteryDrowPrize loDrowPrize,
			ArrayList<Bitmap> bitmapList) {
		Bitmap prizeBGBitmap = loDrowPrize.getPrizeBGBitmap();
		Bitmap prizeBitmap = loDrowPrize.getPrizeBitmap();
		String loDrowPrizeName = loDrowPrize.getName();
		Bitmap loDrowPrizeNumBitmap = loDrowPrize.getNumBitmap();
		if (prizeBGBitmap == null || prizeBitmap == null
				|| loDrowPrizeName == null || loDrowPrizeNumBitmap == null) {
			return null;
		}
		Bitmap showPrizeBitmap = Bitmap.createBitmap(prizeBGBitmap).copy(
				Config.ARGB_4444, true);
		Canvas mCanvas = new Canvas(showPrizeBitmap);
		Paint mPaint = new Paint();
		mCanvas.drawBitmap(prizeBitmap, 0, 0, mPaint);
		mCanvas.drawBitmap(loDrowPrizeNumBitmap, prizeBGBitmap.getWidth()
				- loDrowPrizeNumBitmap.getWidth(),
				prizeBGBitmap.getHeight() * 8 / 15, mPaint);
		// 绘制文字
		mPaint.reset();
		mPaint.setTextSize(LotteryDrowMainView.fontSize5);
		mPaint.setColor(Color.WHITE);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mPaint.setAntiAlias(true);
		float txtPosX = (prizeBGBitmap.getWidth() - mPaint
				.measureText(loDrowPrizeName)) / 2;
		float txtPosY = prizeBGBitmap.getHeight() * 25 / 27;
		mCanvas.drawText(loDrowPrizeName, txtPosX, txtPosY, mPaint);
		if (bitmapList == null) {
			bitmapList = new ArrayList<Bitmap>();
		}
		if (showPrizeBitmap != null) {
			bitmapList.add(showPrizeBitmap);
		}
		return showPrizeBitmap;
	}

	/**
	 * 用于计算图片所处的位置和坐标
	 * 
	 * @param loDrowPrizes
	 *            所有奖品实例
	 * @param defaultImgWidth
	 *            缺省的图片宽度
	 * @param defaultImgHeight
	 *            缺省的图片高度
	 * @param interval
	 *            图片间的间隔
	 * @param leftMargin
	 * @param topMargin
	 * @return 返回的坐标数组，float[0]表示x坐标 float[1]表示y坐标
	 * 
	 */
	public static void setImgPosition(ArrayList<LotteryDrowPrize> loDrowPrizes,
			float defaultImgWidth, float defaultImgHeight, float interval,
			float leftMargin, float topMargin) {
		if (loDrowPrizes == null) {
			return;
		}
		float xInterval = interval * LotteryDrowActivity.widthScale;
		float yInterval = interval * LotteryDrowActivity.heightScale;
		for (int i = 0; i < LotteryDrowView.LOTTERY_PRIZE_NUM; i++) {
			LotteryDrowPrize loDrowPrize = new LotteryDrowPrize(i + 1);
			if (i >= 0 && i <= 5) {
				loDrowPrize.setImgPosX(i * defaultImgWidth + leftMargin
						+ (i + 1) * xInterval);
				loDrowPrize.setImgPosY(topMargin + yInterval);
				loDrowPrize.setTxtPosX(i * defaultImgWidth + leftMargin
						+ (i + 1) * xInterval);
				loDrowPrize.setTxtPosY(defaultImgHeight * 25 / 27 + topMargin
						+ yInterval);
				loDrowPrize.setNumPosX((i + 1) * defaultImgWidth + leftMargin
						+ (i + 1) * xInterval);
				loDrowPrize.setNumPosY(topMargin + defaultImgHeight * 8 / 15
						+ yInterval);
				loDrowPrize.setMultiNumPosX(i * defaultImgWidth + leftMargin
						+ (i + 1) * xInterval);
				loDrowPrize.setMultiNumPoxY(topMargin + defaultImgHeight * 0.5f
						+ yInterval);
				loDrowPrizes.add(loDrowPrize);
				// Log.i(TAG,
				// "imgPosX" + loDrowPrize.getImgPosX() + "  imgPosY"
				// + loDrowPrize.getImgPosY() + "  txtPosX"
				// + loDrowPrize.getTxtPosX() + " txtPosY"
				// + loDrowPrize.getTxtPosY() + " numPosX"
				// + loDrowPrize.getNumPosX() + " numPosY"
				// + loDrowPrize.getNumPosY() + " animNumPosX"
				// + loDrowPrize.getMultiNumPosX()
				// + "  animNumPosY"
				// + loDrowPrize.getMultiNumPoxY());
				continue;
			}
			if (i >= 6 && i <= 8) {
				loDrowPrize.setImgPosX(5 * defaultImgWidth + leftMargin + 6
						* xInterval);
				loDrowPrize.setImgPosY((i - 5) * defaultImgHeight + (i - 5)
						* yInterval + topMargin);
				loDrowPrize.setTxtPosX(5 * defaultImgWidth + leftMargin + 6
						* xInterval);
				loDrowPrize.setTxtPosY((i - 5) * defaultImgHeight
						+ defaultImgHeight * 25 / 27 + topMargin + (i - 5)
						* yInterval);
				loDrowPrize.setNumPosX(6 * defaultImgWidth + leftMargin + 6
						* xInterval);
				loDrowPrize.setNumPosY((i - 5) * defaultImgHeight + topMargin
						+ defaultImgHeight * 8 / 15 + (i - 5) * yInterval);
				loDrowPrize.setMultiNumPosX(5 * defaultImgWidth + leftMargin
						+ 6 * xInterval);
				loDrowPrize.setMultiNumPoxY((i - 5) * defaultImgHeight
						+ topMargin + defaultImgHeight * 0.5f + (i - 5)
						* yInterval);
				loDrowPrizes.add(loDrowPrize);
				// Log.i(TAG,
				// "imgPosX" + loDrowPrize.getImgPosX() + "  imgPosY"
				// + loDrowPrize.getImgPosY() + "  txtPosX"
				// + loDrowPrize.getTxtPosX() + " txtPosY"
				// + loDrowPrize.getTxtPosY() + " numPosX"
				// + loDrowPrize.getNumPosX() + " numPosY"
				// + loDrowPrize.getNumPosY() + " animNumPosX"
				// + loDrowPrize.getMultiNumPosX()
				// + "  animNumPosY"
				// + loDrowPrize.getMultiNumPoxY());
				continue;
			}
			if (i >= 9 && i <= 13) {
				loDrowPrize.setImgPosX((13 - i) * defaultImgWidth + leftMargin
						+ (14 - i) * xInterval);
				loDrowPrize.setImgPosY(3 * defaultImgHeight + topMargin + 4
						* yInterval);
				loDrowPrize.setTxtPosX((13 - i) * defaultImgWidth + leftMargin
						+ (14 - i) * xInterval);
				loDrowPrize.setTxtPosY(3 * defaultImgHeight + topMargin
						+ defaultImgHeight * 25 / 27 + 4 * yInterval);
				loDrowPrize.setNumPosX((14 - i) * defaultImgWidth + leftMargin
						+ (14 - i) * xInterval);
				loDrowPrize.setNumPosY(3 * defaultImgHeight + topMargin
						+ defaultImgHeight * 8 / 15 + 4 * yInterval);
				loDrowPrize.setMultiNumPosX((13 - i) * defaultImgWidth
						+ leftMargin + (14 - i) * xInterval);
				loDrowPrize.setMultiNumPoxY((3) * defaultImgHeight + topMargin
						+ defaultImgHeight * 0.5f + 4 * yInterval);
				loDrowPrizes.add(loDrowPrize);
				// Log.i(TAG,
				// "imgPosX" + loDrowPrize.getImgPosX() + "  imgPosY"
				// + loDrowPrize.getImgPosY() + "  txtPosX"
				// + loDrowPrize.getTxtPosX() + " txtPosY"
				// + loDrowPrize.getTxtPosY() + " numPosX"
				// + loDrowPrize.getNumPosX() + " numPosY"
				// + loDrowPrize.getNumPosY() + " animNumPosX"
				// + loDrowPrize.getMultiNumPosX()
				// + "  animNumPosY"
				// + loDrowPrize.getMultiNumPoxY());
				continue;
			}
			if (i >= 14 & i <= 15) {
				loDrowPrize.setImgPosX(leftMargin + xInterval);
				loDrowPrize.setImgPosY((16 - i) * defaultImgHeight + topMargin
						+ (16 - i) * yInterval);
				loDrowPrize.setTxtPosX(leftMargin + xInterval);
				loDrowPrize.setTxtPosY((16 - i) * defaultImgHeight + topMargin
						+ defaultImgHeight * 25 / 27 + (16 - i) * yInterval);
				loDrowPrize
						.setNumPosX(leftMargin + defaultImgWidth + xInterval);
				loDrowPrize.setNumPosY((16 - i) * defaultImgHeight + topMargin
						+ defaultImgHeight * 8 / 15 + (16 - i) * yInterval);
				loDrowPrize.setMultiNumPosX(leftMargin + xInterval);
				loDrowPrize.setMultiNumPoxY((16 - i) * defaultImgHeight
						+ topMargin + defaultImgHeight * 0.5f + (16 - i)
						* yInterval);
				loDrowPrizes.add(loDrowPrize);
				// Log.i(TAG,
				// "imgPosX" + loDrowPrize.getImgPosX() + "  imgPosY"
				// + loDrowPrize.getImgPosY() + "  txtPosX"
				// + loDrowPrize.getTxtPosX() + " txtPosY"
				// + loDrowPrize.getTxtPosY() + " numPosX"
				// + loDrowPrize.getNumPosX() + " numPosY"
				// + loDrowPrize.getNumPosY() + " animNumPosX"
				// + loDrowPrize.getMultiNumPosX()
				// + "  animNumPosY"
				// + loDrowPrize.getMultiNumPoxY());
				continue;
			}
		}
	}

	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @param config
	 * @param isAssert
	 * @return
	 */
	public static Bitmap readBitmap(Context context, String imgPath,
			Bitmap.Config config, boolean isAssert) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = config;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// opt.inSampleSize = 2;
		// 获取资源图片
		if (isAssert) {
			InputStream is = null;
			try {
				is = context.getAssets().open(imgPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return BitmapFactory.decodeStream(is, null, opt);
		} else {
			return BitmapFactory.decodeFile(imgPath, opt);
		}
	}

	public static TimerTask task = null;
	public static Timer time = null;

	/**
	 * 
	 * @param startLedouNo
	 *            开始乐豆数
	 * @param endLedouNo
	 *            结束乐豆数
	 * @param mContext
	 *            上下文 用于刷新乐豆界面
	 * @param mHandler
	 *            刷新界面
	 */
	public static void ledouAnim(final int startLedouNo, final int endLedouNo,
			final Handler mHandler) {
		if (startLedouNo < 0 || endLedouNo < 0) {
			return;
		}
		if (startLedouNo == endLedouNo) {
			Message msg = mHandler.obtainMessage();
			msg.what = LotteryDrowMainView.HANDLER_REFRESH_BEAN;
			msg.obj = startLedouNo;
			mHandler.sendMessage(msg);
			return;
		}
		task = null;
		time = null;
		task = new TimerTask() {

			int tempLedouNo = startLedouNo;
			int num = 0;

			@Override
			public void run() {
				int addLedouNo = 0;
				String intervalLedouNo = Math.abs(endLedouNo - tempLedouNo)
						+ "";
				// if(intervalLedouNo.length() == 1){
				// addLedouNo = 2;
				// }else if(intervalLedouNo.length() == 2){
				//
				// }

				for (int i = 0; i < intervalLedouNo.length(); i++) {
					addLedouNo = (int) Math.pow(10, i) + addLedouNo;
				}
				Log.i("addLedouNo", addLedouNo + "");

				num++;
				if (startLedouNo > endLedouNo) {
					tempLedouNo = tempLedouNo - addLedouNo;
				} else if (startLedouNo < endLedouNo) {
					tempLedouNo = tempLedouNo + addLedouNo;
				} else {
					task.cancel();
				}
				if (((startLedouNo > endLedouNo) && (tempLedouNo <= endLedouNo))
						|| (startLedouNo < endLedouNo)
						&& (tempLedouNo >= endLedouNo)) {
					tempLedouNo = endLedouNo;
					Message msg = mHandler.obtainMessage();
					msg.what = LotteryDrowMainView.HANDLER_REFRESH_BEAN;
					msg.obj = tempLedouNo;
					mHandler.sendMessage(msg);
					task.cancel();
					return;
				}
				Message msg = mHandler.obtainMessage();
				msg.what = LotteryDrowMainView.HANDLER_REFRESH_BEAN;
				msg.obj = tempLedouNo;
				mHandler.sendMessage(msg);
				Log.i("startLedouNo", startLedouNo + "");
				Log.i("endLedouNo", endLedouNo + "");
				Log.i("ledouNo", tempLedouNo + "");
			}
		};
		time = new Timer();
		time.schedule(task, 10, 100);
	}

}
