package com.mykj.andr.ui;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

public class VersionInfoActivity extends Activity {
	private TextView tvUpdateInfo;
	private TextView textTest;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.version_avtivity);
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.verinfo);
		findViewById(R.id.tvBack).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				finish();
			}
		});

		textTest = (TextView) findViewById(R.id.textTest);
		textTest.setText(getAppInfo());

		tvUpdateInfo = (TextView) findViewById(R.id.verInfo);
		tvUpdateInfo.setText(AppConfig.versionInfo);
	}


	private String getAppInfo(){
		Resources resource = this.getResources();
		StringBuilder sb=new StringBuilder();

		sb.append(resource.getString(R.string.ddz_version) + "： V");
		sb.append(Util.getVersionName(this));
		sb.append('\n');
		if(!Util.isEmptyStr(AppConfig.buildTime)){
			sb.append(resource.getString(R.string.ddz_update_date) + "：");
			sb.append(AppConfig.buildTime);
		}
		return sb.toString();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AnalyticsUtils.onPageStart(this);
		AnalyticsUtils.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AnalyticsUtils.onPageEnd(this);
		AnalyticsUtils.onPause(this);
	}
}