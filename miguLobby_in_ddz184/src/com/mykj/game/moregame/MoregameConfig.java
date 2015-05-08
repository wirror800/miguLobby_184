package com.mykj.game.moregame;

import android.content.Context;

import com.mykj.andr.model.HallDataManager;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

public class MoregameConfig
{
	
	public static final String IP_CONFIG_FILE_NAME = Util.getSdcardPath() + "/" + "downip.ini";
	
	/**模块主路径*/
	public static final String PARENT_PATH = Util.getSdcardPath() + "/" + AppConfig.DOWNLOAD_FOLDER + "/moregames/";
	
	/**图片存储路径*/
	public static final String ICONS_PATH = PARENT_PATH + "/icons";	
	
	/**交叉推广版本，与服务器交互*/
	public static final int MORE_GAME_VER = 2;
	
	/**应用存储路径*/
	private static String APKS_PATH;
	
	/**服务器信息存储路径*/
	private static String SERVICE_DOWNLOAD_PATH;
	
	/**本地奖励保存全路径*/
	private static String REWARD_FILENAME;
	
	
	/**
	 * 初始化路径，因为userID会在切换账号时改变，所以不能写成final的
	 */
	public static void initPath(Context context){
		
		
		/**应用存储路径*/
		APKS_PATH = PARENT_PATH + "/"
				+ AppConfig.clientID + "/" + AppConfig.childChannelId + "/"
				+ HallDataManager.getInstance().getUserMe().userID + "/apks";
		
		/**服务器信息存储路径*/
		SERVICE_DOWNLOAD_PATH = context.getFilesDir() + "/"
				+ AppConfig.clientID + "/" + AppConfig.childChannelId + "/"
				+ HallDataManager.getInstance().getUserMe().userID + "/download";
		
		/**本地奖励保存全路径*/
		REWARD_FILENAME = PARENT_PATH + "/"
				+ AppConfig.clientID + "/" + AppConfig.childChannelId + "/"
				+ HallDataManager.getInstance().getUserMe().userID + "/reward.properties";
	}
	
	public static String APKS_PATH(){
		return APKS_PATH;
	}
	
	/**服务器信息存储路径*/
	public static String SERVICE_DOWNLOAD_PATH(){
		return SERVICE_DOWNLOAD_PATH;
	}
	
	/**本地奖励保存全路径*/
	public static String REWARD_FILENAME(){
		return REWARD_FILENAME;
	}
}
