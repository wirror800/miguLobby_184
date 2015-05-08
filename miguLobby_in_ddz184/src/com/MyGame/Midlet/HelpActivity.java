package com.MyGame.Midlet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.MyGame.Midlet.service.HelpInfo;
import com.MyGame.Midlet.service.MykjService.MykjServiceBinder;
import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;

public class HelpActivity extends Activity {
	private static final String TAG="HelpActivity";

	private String phonenum;

	private Context mContext; 
	private TextView tvBack;
	private TextView tvAdvice;
	private ImageButton btnCallPhone;

	private MykjServiceBinder myService = null; 
	private boolean binded;

	private TextView tvCopyRight;
	private TextView tvQQ;
	private TextView tvQQGroup;
	private TextView tvEmail;



	/**
	 * service 通知Main UI handler
	 */
	public Handler mCustomInfoHandler = new Handler() {  
		@Override
		public void handleMessage(Message msg) {  
			Log.v(TAG,"mCustomInfoHandler ,msg.what="+msg.what);
			switch (msg.what) { 
			case MyGameMidlet.GET_CUSTOMINFO_SUCCESS:  
				loadCustomInfo();
				break;
			case MyGameMidlet.GET_CUSTOMINFO_FAIL:  
				loadLocalInfo();
				break;
			case MyGameMidlet.GET_CUSTOMINFO_VERSION_UPDATE: 
				loadCustomInfo();
				break;	

			}
		}
	};	


	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		mContext=this;
		setContentView(R.layout.activity_help); 


		tvCopyRight=(TextView)findViewById(R.id.tvCopyRight);
		tvQQ=(TextView)findViewById(R.id.tvQQ);
		tvQQGroup=(TextView)findViewById(R.id.tvQQGroup);
		tvEmail=(TextView)findViewById(R.id.tvEmail);



		tvBack=(TextView)findViewById(R.id.tvBack);
		tvBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		tvAdvice=(TextView)findViewById(R.id.tvAdvice);
		tvAdvice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//跳转到反馈界面
				startActivity(new Intent(mContext, FeedbackInfoActivity.class));
			}
		});

		btnCallPhone=(ImageButton)findViewById(R.id.btnCallPhone);
		btnCallPhone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phonenum));  
				startActivity(intent); 		
			}
		});

		Intent intent = new Intent();		
		intent.setAction("mykj.service.BOOT_SERVICE"); 
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		//loadCustomInfo();


	}


	
    @Override
    public void onResume(){
    	super.onResume();
    	if(MyGameMidlet.getIsLoginStatus()!=3){
    		tvAdvice.setVisibility(View.INVISIBLE);
    	}else{
    		tvAdvice.setVisibility(View.VISIBLE);
    	}
    }
	
	
	private ServiceConnection serviceConnection = new ServiceConnection(){   
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myService =(MykjServiceBinder)service; 
			myService.serviceHttpGetCustomInfo(mCustomInfoHandler);
			binded = true; 
			Log.v(TAG, "Service Connected...");

		}

		// 连接服务失败后，该方法被调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			myService = null;
			binded = false; 
			Log.e(TAG, "Service Failed...");

		}
	};



	/**
	 * 加载custom info
	 */
	private void loadCustomInfo() {	
		String copyright=Configs.getVersionName(mContext);
		phonenum = HelpInfo.getInstance().getPhoneNum();
		String qq = HelpInfo.getInstance().getQQNum();
		String qqgroup = HelpInfo.getInstance().getQQGroup();
		String email =  HelpInfo.getInstance().getEmail();
		
		tvCopyRight.setText("版本号:"+copyright);
		tvQQ.setText("客服QQ:"+qq);
		tvQQGroup.setText("QQ群"+qqgroup);
		tvEmail.setText("Email:"+email);

	}
	
	/**
	 * 加载custom info
	 */
	private void loadLocalInfo() {	
		String copyright=Configs.getVersionName(mContext);
		phonenum = getNotifiySharedPreferences("phoneNum","075586219039");
		String qq = getNotifiySharedPreferences("qqNum","7541813101");
		String qqgroup = getNotifiySharedPreferences("qqGroup","23354213");
		String email = getNotifiySharedPreferences("email","apgamehelp@139.com");

		tvCopyRight.setText("版本号:"+copyright);
		tvQQ.setText("客服QQ:"+qq);
		tvQQGroup.setText("QQ群"+qqgroup);
		tvEmail.setText("Email:"+email);
	}
	
		
	/**
	 * 读取SharedPreferences数据
	 * @return String value
	 * */    
	private  String getNotifiySharedPreferences(String key, String defaultValue){
		SharedPreferences sharedPref = getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return sharedPref.getString(key, defaultValue);
	}
	
	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
	}
}
