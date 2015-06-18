package com.mykj.game.moregame;

import java.io.File;
import java.math.BigDecimal;

import org.xmlpull.v1.XmlPullParser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.DisplayMetrics;

import com.MyGame.Migu.R;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;

public class AppVersion {
	private static final String TAG = "AppVersion";

	private Context mContext;
	// ==============================来自http站点的配置信息==============================
	/** 游戏id */
	private int id = -1;

	/** 游戏名中文描述。用于大厅游戏列表中，提供给用户显示。如：斗地主。 */
	private String gameName = null;
	
	/** 游戏描述 */
	private String desc = null;
	/**
	 * 客户端未安装该游戏时，大厅游戏列表中要显示的该游戏的图标。图标命名规则示例：gameName_icon_Ver1.2.4.png。
	 * 此处的版本号指的是图标版本号。与程序版本号无关。
	 */
	private String iconUrl = null;

	/** 游戏入口Activity所在的包名。不能以“.”结尾 */
	private String packageName = null;
	
	/** 游戏入口Activity的类名。以“.”开头 */
	private String activity = null;

	/** 最新版更新地址 */
	private String downloadUrl = null;
	/** 最新版apk文件大小 */
	private String apkSize = null;
	
	/**奖励数量*/
	private int rewardCount = 0;

	/**奖励道具，1.6.3新增，当rewardProp有效时以rewardProp为准，rewardCount无效，否则用rewardCount*/
	private String rewardProp = null;
	
	/** 下载apk文件的MD5校验码 */
	private String apkMD5 = "";

	// ==============================需要分析出来的==============================
	/** 图标名 */
	private String iconName = null;

	/** 图标的实例 */
	private Drawable iconDrawable = null;

	/** apk文件名 */
	private String apkFileName = null;

	/** apk文件存储路径 */
	private String apkFilePath = null;
	
	//控制状态
	private int ctrlStatus = 0;
	
	//新状态
	private final int CTRL_STATUS_NEW = 0x0;
	
	//已领取奖励状态
	private final int CTRL_STATUS_REWARD = 0x2;
	
	private final int CTRL_STATUS_WAP = 0x4;
	
	private final int CTRL_STATUS_NOTIFYED = 0x8;

	/** 游戏下载进度 */
	public int mProgress = 0;

	public boolean isAppInstalled() {
		return Util.isActivityInstalled(mContext, packageName, activity);
	}

	public void setReward(boolean rewarded) {
		setByteFlag(rewarded, CTRL_STATUS_REWARD);
	}

	public boolean isReward() {
		return getByteFlag(CTRL_STATUS_REWARD);
	}

	public boolean isWap(){
		return getByteFlag(CTRL_STATUS_WAP);
	}
	
	
	/**
	 * 成功通知服务器以下载
	 */
	public void setNotifyed(){
		setByteFlag(true, CTRL_STATUS_NOTIFYED);
	}
	
	public boolean isNotifyed(){
		return getByteFlag(CTRL_STATUS_NOTIFYED);
	}
	/**
	 * 设置状态
	 * @param add true增加状态，false去掉状态
	 * @param flag 状态标识
	 */
	private void setByteFlag(boolean add, int flag) {
		if (add) {
			ctrlStatus |= flag;
		} else {
			ctrlStatus &= ~flag;
		}
	}
	
	/**
	 * 获得是否处于状态
	 * @param flag 状态标识
	 * @return
	 */
	private boolean getByteFlag(int flag) {
		return (ctrlStatus & flag) != CTRL_STATUS_NEW;
	}

	/**
	 * 构造
	 */
	public AppVersion(Context context, XmlPullParser p) {
		mContext = context;
		ctrlStatus = CTRL_STATUS_NEW;
		// ==============================来自http站点的配置信息=======================
		getXmlInfo(p);
		// ==============================需要分析出来的==============================
		parseOtherInfo();

	}

	/**
	 * 获得xml数据
	 * @param p
	 */
	private void getXmlInfo(XmlPullParser p) {

		try {
			id = Integer.parseInt(p.getAttributeValue(null, "id"));
		} catch (Exception e) {
			id = -1;
		}

		//游戏名字
		gameName = p.getAttributeValue(null, "descName");
		
		//游戏简介
		desc = p.getAttributeValue(null, "desc");
		
		//图标下载地址
		iconUrl = p.getAttributeValue(null, "iconUrl");

		//游戏包名
		packageName = p.getAttributeValue(null, "package");
		if (packageName.endsWith(".")) {
			packageName = packageName.substring(0, packageName.length() - 1);
		}
		
		//游戏启动activity
		activity = p.getAttributeValue(null, "activity");
		if (!activity.startsWith(".")) {
			activity = "." + activity;
		}
		
		//游戏下载地址
		downloadUrl = p.getAttributeValue(null, "updateUrl");

		// 游戏大小，转化成M
		apkSize = p.getAttributeValue(null, "apkSize");
		long size = 0;
		try {
			size = Long.parseLong(apkSize);//处理掉异常，防止因配置不对引起客户端崩溃
		} catch (Exception e1) {
		}
		float ft = ((float) size) / (1024 * 1024);
		int scale = 2;// 设置位数
		int roundingMode = 4;// 表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
		BigDecimal bd = new BigDecimal((double) ft);
		bd = bd.setScale(scale, roundingMode);
		ft = bd.floatValue();
		apkSize = ft + "M";

		//校验码
		apkMD5 = p.getAttributeValue(null, "apkMD5");
		try {
			rewardCount = Integer.parseInt(p.getAttributeValue(null,
					"rewardBean"));
		} catch (Exception e) {
			rewardCount = 0;
		}
		try{
			rewardProp = p.getAttributeValue(null, "reward");
		} catch (Exception e){
			rewardProp = null;
		}
		//是否已领取奖励
		String isRwd = p.getAttributeValue(null, "isReward");
		if ("true".equals(isRwd) || (rewardCount == 0 && Util.isEmptyStr(rewardProp))) {
			setReward(true);
		}
		
		//这是一个wrap连接
		if(size == 0 || apkMD5 == null || apkMD5.trim().length() == 0){
			setByteFlag(true, CTRL_STATUS_WAP);
		}
		if(isWap() && isReward()){
			setNotifyed();
		}
	}


	/**
	 * 解析其他非直接有xml获得的数据
	 */
	private void parseOtherInfo() {

		iconName = Util.getFileNameFromUrl(iconUrl);

		if (Util.isMediaMounted()) {
			try {
				final String fileName = MoregameConfig.ICONS_PATH + "/"+ iconName;
				File iconFile = new File(fileName);
				boolean needDownload = true;
				if (iconFile.exists() && iconFile.isFile()) { // 已存在icon
					iconDrawable = Util.getDrawableFromFile(mContext, iconFile,
							DisplayMetrics.DENSITY_HIGH);
					if(Util.isDrawableAvailable(iconDrawable)){
						needDownload = false;
					}else{
						iconDrawable = null;
						iconFile.delete();
					}
					
				} 
				if(needDownload) { //不存在，下载
					new Thread() {
						@Override
						public void run() {
							boolean needDownload = true;
							do{
								if (Util.downloadResByHttp(iconUrl, fileName)) {
									iconDrawable = Util.getDrawableFromFile(
											mContext, new File(fileName),
											DisplayMetrics.DENSITY_HIGH);
									Message msg = ((MoregameActivity) mContext).adapterHandler
											.obtainMessage();
									msg.what = MoregameActivity.UPDATE_ICON;
									((MoregameActivity) mContext).adapterHandler
											.sendMessage(msg);
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
							}while(needDownload && Util.isNetworkConnected(mContext));
						}
					}.start();
				}

			} catch (Exception e) {
				Log.v(TAG, "icon trouble is " + e.getMessage());
			}
		}
		//apkFileName = Util.getFileNameFromUrl(downloadUrl);
		apkFileName = id + ".apk";
		apkFilePath = MoregameConfig.APKS_PATH();
	}

	/**
	 * 获得奖励数量
	 * @return
	 */
	public int getReward() {
		return rewardCount;
	}
	
	public String getRewardProp(){
		return rewardProp;
	}
	
	/**
	 * 获得游戏大小，M为单位的字符串
	 * @return
	 */
	public String getApkSize() {
		return apkSize;
	}

	/**
	 * 获得描述
	 * @return
	 */
	public String getGameDesc() {
		return desc;
	}

	/**
	 * 获得文件路径
	 * @return
	 */
	public String getAPKFilePath() {
		return apkFilePath;
	}

	public String getAPKFileName(){
		return apkFileName;
	}
	
	/**
	 * 获得游戏图标，可能是默认图标
	 * @return
	 */
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


	/**
	 * 获取游戏名称
	 * @return
	 */
	public String getGameName() {
		return gameName;
	}

	/**
	 * 获取游戏校验码
	 * @return
	 */
	public String getDownFileConfigMD5() {
		return apkMD5;
	}

	/**
	 * 获取游戏id
	 * @return
	 */
	public int getGameId() {
		return id;
	}

	/**
	 * 游戏下载路径
	 * */
	public String getDownloadUrl() {
		return downloadUrl;
	}

	/**
	 * 游戏是否已经下载完成
	 * */
	public boolean isDownloaded() {
		boolean isDownload = false;
		//apkFileName = Util.getFileNameFromUrl(downloadUrl);
		if (apkFileName != null && apkFileName.trim().length() > 0) {
			File apkFile = new File(apkFilePath, apkFileName);
			if (apkFile.exists() && apkFile.isFile())// 已经下载完了哦。
			{
				if (Util.downloadFileMD5Check(apkFile, apkMD5)) {
					isDownload = true;
				} else {
					apkFile.delete();
				}
			}
		}
		return isDownload;
	}

	public void runApk(){
		Intent in=new Intent();
		ComponentName comp = new ComponentName(packageName,packageName + activity);
		in.setComponent(comp);
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(in);
	}
	
}
