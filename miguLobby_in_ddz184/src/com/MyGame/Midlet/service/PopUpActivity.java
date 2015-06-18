package com.MyGame.Midlet.service;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.MyGame.Migu.MyGameMidlet;
import com.MyGame.Migu.R;
import com.MyGame.Midlet.util.AppConfig;


public class PopUpActivity extends Activity {
	private Context mContext;
	private TextView tvContent;
	private TextView tvYES;
	private TextView tvNO;
	private Intent mIntent;
	private Bundle mBundle;

	private	int mAt;                //=mIntent.getExtras().getInt("at");
	private	String mUri;         //=mIntent.getExtras().getString("uri");
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup_activity); 
		mContext=this;
		mIntent=getIntent();
		mBundle=getIntent().getExtras();
	//	Intent intent=getIntent();
		
		String content=mIntent.getExtras().getString("content");
		mAt=mIntent.getExtras().getInt("at");
		mUri=mIntent.getExtras().getString("uri");
		
		tvContent = (TextView)findViewById(R.id.tvContent);
		tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());

		tvContent.setText("        "+content);
		tvYES= (TextView)findViewById(R.id.tvYES); 
		tvYES.setOnClickListener(new EntryKenOnClickListener());
		tvNO= (TextView)findViewById(R.id.tvNO); 
		tvNO.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();				
			}
		});
	}

/*	@Override
	protected void onNewIntent(Intent intent) {
		if(intent!=null){
			String content=intent.getExtras().getString("content");
			mAt=intent.getExtras().getInt("at");
			mUri=intent.getExtras().getString("uri");
		}
	}
	*/

	private class EntryKenOnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Intent in=new Intent();

			if(1==mAt){  //在浏览器中打开uri定义的网页
				Uri uri = Uri.parse(mUri); 
				in.setAction(Intent.ACTION_VIEW);
				in.setData(uri);

			}else if(2==mAt){//打开大厅

				in.setClass(mContext, MyGameMidlet.class);

			}else if(3==mAt){  //打开uri指定的应用名, 如果为空则提示下载
				//判断游戏是否安装	
				String activityName=mUri.trim();
				String[] s = mUri.split("\\.");
				StringBuilder sb=new StringBuilder();
				for(int i=0;i<s.length-2;i++){
					sb.append(s[i]);
					sb.append('.');
				}
				sb.append(s[s.length-2]);

				String packageName=sb.toString().trim();

				if(isActivityInstalled(mContext,packageName,activityName)){

					Bundle bundle = new Bundle();
					bundle.putString("CHANNEL_ID",AppConfig.channelId);
					bundle.putString("FID", AppConfig.fid);
					bundle.putString("CHILD_CHANNEL_ID", AppConfig.childChannelId );
					ComponentName comp = new ComponentName(packageName,activityName);
					in.putExtras(bundle);
					in.setComponent(comp);
				}else if(isMergeGame(packageName)){
					Bundle bundle = new Bundle();
					bundle.putString("CHANNEL_ID",AppConfig.channelId);
					bundle.putString("FID", AppConfig.fid);
					bundle.putString("CHILD_CHANNEL_ID", AppConfig.childChannelId );
					ComponentName comp = getStartAct(packageName);
					in.putExtras(bundle);
					if(comp!=null){
						in.setComponent(comp);
					}else{
						in.setClass(mContext, MyGameMidlet.class);
					}
				}else{//应用没有安装
					in.setClass(mContext, MyGameMidlet.class);
				}
			}
			startActivity(in);
			finish();
		}
	}


	private boolean isMergeGame(String pkgName){
		int id=-1;
		if(pkgName.equals("com.mykj.game.ddz")){
			id=100;
		}
		
		for(String[] strs:AppConfig.LocalGameConfigs){
			if(id==Integer.parseInt(strs[0])){
				return true;
			}
		}			
		return false;
	}
	
	
	private ComponentName getStartAct(String pkgName){
		int id=-1;
		String packageName="";
		String startAct="";
		if(pkgName.equals("com.mykj.game.ddz")){
			id=100;
		}
		for(String[] strs:AppConfig.LocalGameConfigs){
			if(id==Integer.parseInt(strs[0])){
				//String gameVer=strs[1];
				packageName=mContext.getPackageName();
				startAct=packageName+strs[2];
			}
		}			
		if(packageName.length()>0&&startAct.length()>0){
			return new ComponentName(packageName,startAct);
		}
		return null;
}
	
	
	/**package acitivity 是否安装*/
	private boolean isActivityInstalled(Context context, String packageName, String activityName)
	{
		if (packageName == null || packageName.trim().length() == 0)
		{
			return false;
		}

		if (activityName == null || activityName.trim().length() == 0)
		{
			return false;
		}
		try
		{
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			for (int i = 0; i < pi.activities.length; i++)
			{
				ActivityInfo ai = pi.activities[i];
				if (ai.name.equals(activityName))
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{

		}
		return false;
	}
}
