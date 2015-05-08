package com.mykj.game;

import java.util.Stack;

import android.app.Activity;
import android.app.Application;

import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;

/***
 * 当前接入斯凯的支付方式，所以继承自PayApplication，否则继承自Application
 * @ClassName: MainApplication
 * @Description: 应用程序全局类，相当于cocos2d-x的AppDelegate,或者IOS的CCApplication
 * @date 2013-4-25 下午03:39:00
 *
 */
public class MainApplication {

	private static final String TAG="MainApplication";
	private static MainApplication instance;

	private static Stack<Activity> mActivityStack;
	

//	@Override
//	public void onCreate() {
//		AppConfig.initCmccSwitch(this);
//
////		if(AppConfig.isOpenPayByCmccSdk()){
////			System.loadLibrary("megjb");  //移动支付SDK SO
////		}
//	
//		instance=this;
//		super.onCreate();
//	}
	
	public MainApplication(){
		
	}


	public static MainApplication sharedApplication(){
		
		if(instance==null){
			instance=new MainApplication();
		}
		return instance;
	}


	/**
	 * add Activity 添加Activity到栈
	 */
	public void addActivity(Activity activity){
		if(mActivityStack ==null){
			mActivityStack =new Stack<Activity>();
		}
		mActivityStack.add(activity);
	}
	/**
	 * get current Activity 获取当前Activity（栈中最后一个压入的）
	 */
	public Activity currentActivity() {
		Activity activity = mActivityStack.lastElement();
		return activity;
	}
	/**
	 * 结束当前Activity（栈中最后一个压入的）
	 */
	public void finishActivity() {
		Activity activity = mActivityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			mActivityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		for (Activity activity : mActivityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() {
		for (int i = 0, size = mActivityStack.size(); i < size; i++) {
			if (null != mActivityStack.get(i)) {
				mActivityStack.get(i).finish();
			}
		}
		mActivityStack.clear();
	}

	/**
	 * 退出应用程序
	 */
	public void AppExit() {
		try {
			finishAllActivity();
		} catch (Exception e) {
		}
	}

}
