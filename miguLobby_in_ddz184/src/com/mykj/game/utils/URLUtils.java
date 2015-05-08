package com.mykj.game.utils;

public class URLUtils {
	/**
	 * @Title: getVipSettings
	 * @Description: 获得VIP配置地址(德州)
	 * @param channelId
	 * @param gameId
	 * @param version
	 * @return
	 * @version: 2013-6-19 下午02:07:32
	 */
	public static String getVipSettings(String channelId,int gameId,String version){
		StringBuffer sb = new StringBuffer();

		sb.append(AppConfig.VIPHOST);
		sb.append("?channelId=");
		sb.append(channelId); 
		sb.append("&gameid=");
		sb.append(gameId);
		sb.append("&version=");
		sb.append(version);
		return sb.toString();
	}
}
