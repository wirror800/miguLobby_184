package com.MyGame.Migu;

import com.MyGame.Midlet.util.AppConfig;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MyGame.Midlet.service.MykjService.MykjServiceBinder;
import com.MyGame.Migu.R;

public class SettingActivity extends Activity {
	private static final String TAG = "SettingActivity";

	private Context mContext;

	private RelativeLayout relateAccout;
	private RelativeLayout relateChangePW;
	private RelativeLayout relateAdvice;
	private RelativeLayout frameAdvice;
	private RelativeLayout relateHelp;
	private RelativeLayout helpAdvice;

	/** WIFI自动更新 */
	private CheckBox cbWiFiUpdate;

	/** notifiy 提示音 */
	private CheckBox cbNotifiySound;

	/** notifiy 震动 */
	private CheckBox cbNotifiyVibrate;

	private TextView tvBack;

	private MykjServiceBinder myService = null;
	private boolean binded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_setting);
		frameAdvice = (RelativeLayout) findViewById(R.id.frameAdvice);

		relateAccout = (RelativeLayout) findViewById(R.id.relateAccout);
		relateAccout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				if (null != MyGameMidlet.mContext) {
					((MyGameMidlet) MyGameMidlet.mContext).entryAccoutLoginView();
				}
			}
		});

		relateChangePW = (RelativeLayout) findViewById(R.id.relateChangePW);
		relateChangePW.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// startActivity(new Intent(SettingActivity.this,
				// FeedbackInfoActivity.class));
				Toast.makeText(SettingActivity.this, "功能近期上线，敬请期待.",
						Toast.LENGTH_SHORT).show();
				// finish();
				// AccountManager.showModifyPswView(MainLobbyActivity.mContext);
			}
		});
		relateAdvice = (RelativeLayout) findViewById(R.id.relateAdvice);
		relateAdvice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 跳转到反馈界面
				startActivity(new Intent(mContext, FeedbackInfoActivity.class));
			}
		});
		helpAdvice = (RelativeLayout) findViewById(R.id.helpAdvice);

		relateHelp = (RelativeLayout) findViewById(R.id.relateHelp);
		relateHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 跳转到反馈界面
				startActivity(new Intent(mContext, HelpActivity.class));
			}
		});

		if (AppConfig.isGamePlayerEnable) {
			helpAdvice.setVisibility(View.VISIBLE);
		} else {
			helpAdvice.setVisibility(View.GONE);
		}

		// 自动更新
		cbWiFiUpdate = (CheckBox) findViewById(R.id.cbWifi);
		cbWiFiUpdate.setOnCheckedChangeListener(new WifiUpdateListener());
		// 声音
		cbNotifiySound = (CheckBox) findViewById(R.id.cbNotifiySound);
		cbNotifiySound.setOnCheckedChangeListener(new NotifiySoundListener());

		// 震动
		cbNotifiyVibrate = (CheckBox) findViewById(R.id.cbNotifiyVibrate);
		cbNotifiyVibrate
				.setOnCheckedChangeListener(new NotifiyVibrateListener());

		tvBack = (TextView) findViewById(R.id.tvBack);
		tvBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		Intent intent = new Intent();
		intent.setAction("mykj.service.BOOT_SERVICE");
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

	}

	/** wifi自动更新监听器 */
	private class WifiUpdateListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (binded) {
				myService.setBGUpdateState(isChecked);
			}
		}
	}

	/** nofifiy声音监听器 */
	private class NotifiySoundListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (binded) {
				myService.setNotifiySoundState(isChecked);
			}
		}
	}

	/** nofifiy震动监听器 */
	private class NotifiyVibrateListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (binded) {
				myService.setNotifiyVibrateState(isChecked);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (MyGameMidlet.getIsLoginStatus() != 3) {
			frameAdvice.setVisibility(View.GONE);
		} else {
			frameAdvice.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unbindService(serviceConnection);
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myService = (MykjServiceBinder) service;

			boolean updateState = myService.getBGUpdateState();
			boolean soundState = myService.getNotifiySoundState();
			boolean vibrateState = myService.getNotifiyVibrateState();
			cbWiFiUpdate.setChecked(updateState);
			cbNotifiySound.setChecked(soundState);
			cbNotifiyVibrate.setChecked(vibrateState);

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




}
