package com.MyGame.Midlet.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mingyou.accountInfo.LoginInfoManager;


public class MykjReceiver extends BroadcastReceiver{  
	private static final String TAG="MykjReceiver";

	@Override 
	public void onReceive(Context context, Intent intent) {  

		Log.v(TAG, "MykyReceiver is onReceive");   
		/*if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
				||intent.getAction().equals("mykj.intent.action.ALARM_BOOT_BROADCAST")){
			Log.v(TAG,intent.getAction()); 
			Intent in=new Intent();//intent对象 用于启动服务   
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			in.setAction("mykj.service.BOOT_SERVICE");   
			in.setClass(context.getApplicationContext(), MykjService.class);
			context.startService(in);//开机 启动服务              
		}

		else if (intent.getAction().equals("mykj.intent.action.BOOT_BROADCAST")) {      
			Log.v(TAG, "action is mykj.intent.action.BOOT_SERVICE"); 
			Intent in=new Intent();//intent对象 用于启动服务   
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			in.setAction("mykj.service.NOTIFIY_MSG");   
			in.setClass(context.getApplicationContext(), MykjService.class);
			context.startService(in);         			
		}
		*/
		if (intent.getAction().equals("mykj.intent.action.JUST_START_SERVER")) {      
			Log.v(TAG, "mykj.intent.action.JUST_START_SERVER"); 
			Intent in=new Intent();//intent对象 用于启动服务   
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			in.setAction("mykj.service.JUST_BOOT_SERVICE");   
			in.setClass(context.getApplicationContext(), MykjService.class);
			context.startService(in);         			
		}
		else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
				||intent.getAction().equals("mykj.intent.action.ALARM_BOOT_BROADCAST")){
			Log.v(TAG,intent.getAction()); 
			Intent in=new Intent();//intent对象 用于启动服务   
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			in.setAction("mykj.service.BOOT_SERVICE");   
			in.setClass(context.getApplicationContext(), MykjService.class);
			context.startService(in);//开机 启动服务              
		}
		/*else if (intent.getAction().equals("mykj.intent.action.TOKEN_BINDING")) {
			Log.v(TAG, "mykj.intent.action.TOKEN_BINDING"); 
			Intent in=intent;
			String token=in.getStringExtra("token");
			LoginInfoManager.getInstance().updateWhiteName(token);
		}*/
		
	}  

}  