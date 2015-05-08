package com.mykj.game.ddz.api;

import android.content.Context;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

/**
 * 友盟统计
 * @author Administrator
 *
 */
public class AnalyticsUtils {
	
	public static final String umeng_appkey = "5369ead056240b536802054c";
	
	/**
	 * 友盟点击事件，
	 * @param context 
	 * @param eventId 事件id，可以从友盟后台查看对应事件的Id,事件id一定不能弄错
	 */
	public static void onClickEvent(Context context,String eventId){
		MobclickAgent.onEvent(context, eventId);
		Log.i("info", "事件id:"+eventId);
	}
	
	/**
	 * 在activity中onResume中添加
	 * @param context
	 */
	public static void onResume(Context context){
		MobclickAgent.onResume(context);
	}
	/**
	 * 在Activity中onPause中添加
	 * @param context
	 */
	public static void onPause(Context context){
		MobclickAgent.onPause(context);
	}
	
	/**
	 * 页面开始时添加
	 * @param o
	 */
	public static void onPageStart(Object o){
		MobclickAgent.onPageStart(o.getClass().getName());
	}
	public static void onPageStart(String o){
		MobclickAgent.onPageStart(o);
	}
	/**
	 * 页面结束时添加
	 * @param o
	 */
	public static void onPageEnd(Object o){
		MobclickAgent.onPageEnd(o.getClass().getName());
	}
	public static void onPageEnd(String o){
		MobclickAgent.onPageEnd(o);
	}
}
