package com.MyGame.Midlet.service;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import com.MyGame.Midlet.util.Configs;

public class LobbyVersion {

	private Context mContext;
	private String lobbyMaxVer;
	private String lobbyMinVer;
	private String lobbyUpdateUrl;
	private String lobbyApkSize;
	private String lobbyApkMD5;

	public LobbyVersion(Context context, XmlPullParser p)
	{
		mContext=context;
		lobbyMaxVer=p.getAttributeValue(null, "maxVer");
		lobbyMinVer=p.getAttributeValue(null, "minVer");
		lobbyUpdateUrl=p.getAttributeValue(null, "updateUrl");
		lobbyApkSize=p.getAttributeValue(null, "apkSize");
		lobbyApkMD5=p.getAttributeValue(null, "apkMD5");

	}

	
	
	private String getDownLoadFileNamePath(){
		String path=AppVersion.getDownloadPath();
		String name= Configs.getFileNameFromUrl(lobbyUpdateUrl);//从URL中获取文件名
		return path+"/"+name;
	}
	
	
	
	//安装大厅
	public Intent getActionType(){
		Intent intent=new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + getDownLoadFileNamePath()), "application/vnd.android.package-archive");
		return intent;
	}
	public String getDownloadUrl(){
		return lobbyUpdateUrl;
	}

	public String getDownFileConfigMD5(){
		return lobbyApkMD5;
	}


	/**
	 * 游戏是否需要升级
	 * 
	 * */
	public boolean isNeedUpdate(){
		if(getVersionName(mContext).compareTo(lobbyMaxVer)<0){			
			return true;
		}
		return false;
	}


	public static String getVersionName(Context ctx) 
	{
		String version="";

		PackageManager packageManager = ctx.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(ctx.getPackageName(),0);
			version = packInfo.versionName;

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;

	}
}
