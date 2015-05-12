package com.MyGame.Midlet.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.MyGame.Midlet.R;
import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;
import com.login.view.LoginView;

public class AppVersion {
	private static final String TAG = "AppVersion";

	// ==============================来自http站点的配置信息==============================
	/** 游戏id */
	private int id = -1;

	private Context mContext;

	/** 游戏状态hot,new */
	private String status;

	/** 游戏名。限英文字符。必须与*.apk中的GameName一致。详见《Android游戏大厅各游戏接入接口规范》第三节。 */
	private String gameName = null;
	/** 游戏名中文描述。用于大厅游戏列表中，提供给用户显示。如：斗地主。 */
	private String descName = null;
	/** 游戏描述 */
	private String desc = null;
	/**
	 * 客户端未安装该游戏时，大厅游戏列表中要显示的该游戏的图标。图标命名规则示例：gameName_icon_Ver1.2.4.png。
	 * 此处的版本号指的是图标版本号。与程序版本号无关。
	 */
	private String iconUrl = null;

	// private String nameBmpUrl = null; //去掉，未使用
	/** 游戏入口Activity所在的包名。不能以“.”结尾 */
	private String packageName = null;
	/** 游戏入口Activity的类名。以“.”开头 */
	private String activity = null;
	/** 最大版本号 */
	private String maxVer = null;
	/** 最小版本号 */
	private String minVer = null;
	/** 最新版更新地址 */
	private String updateUrl = null;
	/** 最新版升级信息 */
	private String updateInfo = null;
	/** 最新版apk文件大小 */
	private String apkSize = null;

	// ==============================需要分析出来的==============================
	/** 游戏自身app版本号 */
	private String appVer = null;
	/** 是否已经安装 */
	private boolean isInstalled = false;
	/** 图标名 */
	public String iconName = null;

	// 游戏名称图片的名称
	// private String nameBmpName = null; //去掉，未使用
	/** 图标的实例 */
	private Drawable iconDrawable = null;

	// public Drawable nameDrawable = null; //去掉，未使用
	/** apk文件名 */
	private String apkFileName = null;

	/** apk文件存储路径 */
	private String apkFilePath = null;

	// /**apk文件的版本*/
	// public Version apkVer = null;
	/** 下载apk文件的MD5校验码 */
	private String apkMD5 = "";

	/** 是否下载完成 */
	private boolean isDownloadFinished = false;

	// private Drawable imgDownload=null;
	/** 游戏在线人数 */
	private String onLineNum = "";

	/** 集成包游戏版本,从AppConfig.LocalGameConfigs 中获取 */
	private String mLocalAppVer = null;

	private String mLocalActivity = null;
	/** 游戏下载进度 */
	public int mProgress = 0;

	/**
	 * 构造
	 */
	public AppVersion(Context context, XmlPullParser p) {
		// ==============================来自http站点的配置信息==============================
		try {
			id = Integer.parseInt(p.getAttributeValue(null, "id"));
		} catch (Exception e) {
			id = -1;
		}
		mContext = context;

		status = p.getAttributeValue(null, "status");

		gameName = p.getAttributeValue(null, "gameName");
		descName = p.getAttributeValue(null, "descName");
		desc = p.getAttributeValue(null, "desc");
		iconUrl = p.getAttributeValue(null, "iconUrl");
		apkSize = p.getAttributeValue(null, "apkSize");

		// nameBmpUrl = p.getAttributeValue(null, "nameBmpUrl");
		packageName = p.getAttributeValue(null, "package");
		if (packageName.endsWith(".")) {
			packageName = packageName.substring(0, packageName.length() - 1);
		}
		activity = p.getAttributeValue(null, "activity");
		if (!activity.startsWith(".")) {
			activity = "." + activity;
		}
		maxVer = p.getAttributeValue(null, "maxVer");
		minVer = p.getAttributeValue(null, "minVer");
		updateUrl = p.getAttributeValue(null, "updateUrl");
		updateInfo = p.getAttributeValue(null, "updateInfo");
		if (updateInfo != null) {
			updateInfo = updateInfo.trim().replace('#', '\n');
		}

		apkMD5 = p.getAttributeValue(null, "apkMD5");
		// ==============================需要分析出来的==============================
		isInstalled = isActivityInstalled(context, packageName, activity);
		appVer = null;
		if (isInstalled) {
			appVer = getAppVer(context, packageName);
		}

		// nameBmpName =getFileNameFromUrl(nameBmpUrl); //已经未使用
		iconName = Configs.getFileNameFromUrl(iconUrl);

		if (Configs.isMediaMounted()) {
			try {
				final File iconFile = new File(Configs.getSdcardPath()
						+ AppConfig.ICONS_PATH, iconName);
				final File iconFiletmp = new File(Configs.getSdcardPath()
						+ AppConfig.ICONS_PATH, iconName + ".tmp");
				if (iconFile.exists() && iconFile.isFile()) {
					iconDrawable = Configs.getDrawableFromFile(context,
							iconFile, DisplayMetrics.DENSITY_HIGH);

				} else {
					// downloadImgBitmap(iconFile);
					new Thread() {
						@Override
						public void run() {
							// iconDrawable=Configs.downloadImgBitmap(iconUrl,iconFile,mContext);
							if (Configs.downloadImgBitmap(iconUrl, iconFiletmp)) {
								iconFiletmp.renameTo(iconFile);
								iconDrawable = Configs.getDrawableFromFile(
										mContext, iconFile,
										DisplayMetrics.DENSITY_HIGH);
							}
						}
					}.start();
				}

			} catch (Exception e) {

			}
		}
		apkFileName = Configs.getFileNameFromUrl(updateUrl);
		apkFilePath = Configs.getSdcardPath() + AppConfig.APKS_PATH + "/"
				+ apkFileName;

		if (Configs.isEmptyStr(onLineNum)) {
			new Thread() {
				@Override
				public void run() {
					String userdata = Configs
							.getConfigXmlByHttp(getOnLineUrl());
					Log.v(TAG, "userdata" + userdata);
					String num = null;
					try {
						String[] q = userdata.split("#");
						if (q != null && q.length == 3) {
							num = q[2]; // 在线人数
						}
					} catch (NullPointerException e) {

					}
					onLineNum = num;
				}
			}.start();
		}

		// 获取集成包信息
		try {
			if (id != -1) {
				for (String[] strs : AppConfig.LocalGameConfigs) {
					int localGameId = Integer.parseInt(strs[0]);
					if (localGameId == id) {
						mLocalAppVer = strs[1];
						mLocalActivity = mContext.getPackageName() + strs[2];
					}
				}

			}
		} catch (Exception e) {
			Log.e(TAG, "local game config error!");
		}
	}

	public int getGameStatus() {
		int res = 0;
		if (status == null) {
			return 0;
		}
		String str = status.trim();
		if (str.equals("hot")) {
			res = 1;
		} else if (str.equals("new")) {
			res = 2;
		}
		return res;
	}

	public String getApkSize() {
		return apkSize;
	}

	public String getGameDesc() {
		return desc;
	}

	public String getAPKFilePath() {
		return apkFilePath;
	}

	public Drawable getGameIcon() {
		if (iconDrawable == null) {
			Bitmap iconBitmap = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.game_default_icon);
			iconDrawable = new BitmapDrawable(mContext.getResources(),
					iconBitmap);
			return iconDrawable;
		}
		return iconDrawable;
	}

	public String getGameName() {
		// chenqy客户端强行修改应用名称 @2015-05-06
		String gameName = descName.replaceAll("赢话费", "咪咕");
		if (!gameName.startsWith("咪咕")) {
			gameName = "咪咕" + gameName;
		}
		return gameName;
	}

	public String getDownFileConfigMD5() {
		return apkMD5;
	}

	public int getGameId() {
		return id;
	}

	/**
	 * 游戏下载路径
	 * */
	public String getDownloadUrl() {
		return updateUrl;
	}

	/**
	 * 游戏更新notifiy 内容
	 * */
	public String getUpdateContext() {
		String str = "";
		if (updateInfo != null) {
			str = updateInfo;
		} else if (descName != null) {
			str = descName + "wifi后台下载完成";
		}
		return str;
	}

	/**
	 * 游戏更新notifiy 标题
	 * */
	public String getUpdateTitle() {
		String str = "";
		if (descName != null) {
			str = descName;
		}
		return str;
	}

	/**
	 * 游戏是否已经下载完成
	 * */
	public boolean isUpdateComplete() {
		apkFileName = Configs.getFileNameFromUrl(updateUrl);
		if (apkFileName != null && apkFileName.trim().length() > 0) {
			File apkFile = new File(Configs.getSdcardPath()
					+ AppConfig.APKS_PATH, apkFileName);
			if (apkFile.exists() && apkFile.isFile())// 已经下载完了哦。
			{
				if (Configs.downloadFileMD5Check(apkFile, apkMD5)) {
					isDownloadFinished = true;
				} else {
					isDownloadFinished = false;
					apkFile.delete();
				}
			} else {
				isDownloadFinished = false;
			}
		}
		return isDownloadFinished;
	}

	public Intent getActionType() {
		Intent intent = new Intent();
		if (isNeedUpdate()) {
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + apkFilePath),
					"application/vnd.android.package-archive");
		}
		return intent;
	}

	/**
	 * 游戏是否需要升级
	 * 
	 * */
	public boolean isNeedUpdate() {
		boolean res = false;
		if (isAppInstalled()) {
			String ver = getAppVer(mContext, packageName);
			if (mLocalAppVer != null && ver != null) {
				appVer = mLocalAppVer.compareTo(ver) < 0 ? ver : mLocalAppVer;
			} else if (mLocalAppVer == null && ver != null) {
				appVer = ver;
			} else if (mLocalAppVer != null && ver == null) {
				appVer = mLocalAppVer;
			}

			if (appVer.compareTo(maxVer) < 0) {
				res = true;
			}
		}
		return res;

	}

	/**
	 * 游戏是否需要强制升级
	 * 
	 * */
	public boolean isMustUpdate() {
		boolean res = false;
		if (isAppInstalled()) {
			String ver = getAppVer(mContext, packageName);
			if (mLocalAppVer != null && ver != null) {
				appVer = mLocalAppVer.compareTo(ver) < 0 ? ver : mLocalAppVer;
			} else if (mLocalAppVer == null && ver != null) {
				appVer = ver;
			} else if (mLocalAppVer != null && ver == null) {
				appVer = mLocalAppVer;
			}

			if (appVer.compareTo(minVer) < 0) {
				res = true;
			}
		}
		return res;

	}

	/**
	 * 游戏是否安装
	 * */
	public boolean isAppInstalled() {
		isInstalled = isActivityInstalled(mContext, packageName, activity);
		if (mLocalAppVer != null) {
			isInstalled = true;
		}
		return isInstalled;
	}

	/**
	 * 游戏下载目录
	 * */
	public static String getDownloadPath() {
		String path = "";
		if (Configs.isMediaMounted()) {
			path = Configs.getSdcardPath() + AppConfig.APKS_PATH;
		}
		return path;
	}

	/**
	 * 获取游戏包名
	 * */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * 获取游戏包名加activity
	 * */
	public String getEntryActivity() {
		return packageName + activity;
	}

	/**
	 * 启动游戏
	 * */
	public void startGame(Context context) {
		// LoginView.isExistCurAcc = true;
		Intent in = new Intent();
		Bundle bundle = new Bundle();

		bundle.putString("CHANNEL_ID", AppConfig.channelId);
		bundle.putString("FID", AppConfig.fid);
		bundle.putString("CHILD_CHANNEL_ID", AppConfig.childChannelId);
		bundle.putBoolean("NEWLOBBY", true);

		String pkgName = null;
		String startAct = null;
		String ver = getAppVer(mContext, packageName);

		if (mLocalAppVer == null) {
			pkgName = packageName;
			startAct = getEntryActivity();
		} else {
			if (mLocalAppVer != null && ver != null) {
				if (ver.compareTo(mLocalAppVer) > 0) {
					pkgName = packageName;
					startAct = getEntryActivity();
				} else {
					pkgName = mContext.getPackageName();
					startAct = mLocalActivity;
				}
			} else {
				pkgName = mContext.getPackageName();
				startAct = mLocalActivity;
			}
		}
		Log.v(TAG, "pkgName=" + pkgName);
		Log.v(TAG, "startAct=" + startAct);
		ComponentName comp = new ComponentName(pkgName, startAct);
		in.putExtras(bundle);
		in.setComponent(comp);
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(in);

	}

	/**
	 * 检测程序是否已经安装
	 * 
	 * @param packageName
	 *            游戏入口Activity所在的包名。不能以“.”结尾
	 * @param activityName
	 *            游戏入口Activity的类名。以“.”开头
	 */
	private boolean isActivityInstalled(Context context, String packageName,
			String activityName) {
		if (packageName == null || packageName.trim().length() == 0) {
			return false;
		}

		if (activityName == null || activityName.trim().length() == 0) {
			return false;
		}
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES);
			String s = packageName + activityName;
			for (int i = 0; i < pi.activities.length; i++) {
				ActivityInfo ai = pi.activities[i];
				if (ai.name.equals(s)) {
					return true;
				}
			}
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * 获取已安装程序的版本
	 */
	public static String getAppVer(Context context, String packageName) {
		String ver = null;

		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
			ver = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return ver;
	}

	/**
	 * 获取游戏在线人数
	 * */
	public String getOnLineNum() {
		if (id < 0) {
			return "即将上线";
		} else if (Configs.isEmptyStr(onLineNum)
				|| onLineNum.trim().equals("0")) {
			return onLineNum;
		} else {
			return onLineNum + "人";
		}
	}

	private String getOnLineUrl() {
		StringBuffer sb = new StringBuffer();
		sb.append("http://qpwap.cmgame.com/game/getonlineusers.aspx?cid=");// 现网地址
		sb.append(AppConfig.channelId);
		sb.append("02ANDROID1&gameid=");
		sb.append(id);
		return sb.toString();
	}

	public boolean copyApkFromAssets(Context context, String fileName,
			String path) {
		boolean copyIsFinish = false;
		try {
			InputStream is = context.getAssets().open(fileName);
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] temp = new byte[1024];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			fos.close();
			is.close();
			copyIsFinish = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return copyIsFinish;
	}

}
