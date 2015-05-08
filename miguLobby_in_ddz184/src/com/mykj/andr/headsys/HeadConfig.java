package com.mykj.andr.headsys;

import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/**
 * @author Administrator wanghj
 * 头像系统配置
 */
public class HeadConfig{
	private static final String headSavePth = Util.getSdcardPath() + "/" + AppConfig.DOWNLOAD_FOLDER + "/"
			+ "headImg" + "/" + AppConfig.gameId + "/" + AppConfig.clientID + "/";   //头像保存主路径
	public static final String zoneHeadSavePth = headSavePth + "zone" + "/";   //分区用图片
	public static final String gameHeadSavePth = headSavePth + "game" + "/";   //游戏用图片
}