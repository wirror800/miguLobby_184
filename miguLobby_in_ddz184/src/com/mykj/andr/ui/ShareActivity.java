package com.mykj.andr.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.MyGame.Midlet.R;
import com.MyGame.Midlet.wxapi.WXEntryActivity;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;
import com.mykj.game.moregame.MoregameActivity;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class ShareActivity extends Activity {

	private TextView mBack;
	private Button mPopularize;
	private Button mBounsRule;
	private Button mGotoWX;
	private ImageView btnFreeBean;    //免费赚豆按钮

	private TextView mSPtext;

	private Context context;


	private OnClickListener mRuleListener;
	private OnClickListener mPopularizeListener;

	/**
	 * 仅启动动画
	 */
	@SuppressLint("HandlerLeak")
	private Handler startAnimHander = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(btnFreeBean != null){
				AnimationDrawable ad = (AnimationDrawable) btnFreeBean.getBackground();
				ad.stop();
				ad.start();
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.share_activity);
		initializeListener();
		initializeUI();
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
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private void initializeListener() {
		// TODO Auto-generated method stub
		mRuleListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//				String url = UtilHelper.getContainBaseUrl(bounsRuleUrlId,
				//						userToken);
				String url = AppConfig.SHARE_RULEPATH;
				UtilHelper.onWeb(context, url);
			}
		};

		mPopularizeListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(context, PopularizeActivity.class));
			}
		};

	}

	private void initializeUI() {
		// TODO Auto-generated method stub
		mBack = (TextView) findViewById(R.id.tvBack);
		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.share_to_friends);
		mPopularize = (Button) findViewById(R.id.my_popularize);
		mBounsRule = (Button) findViewById(R.id.bouns_rule);
		mGotoWX = (Button) findViewById(R.id.goto_popularize);
		mSPtext = (TextView) findViewById(R.id.spread_key_text);
		if (!Util.isEmptyStr(AppConfig.spKey)) {
			mSPtext.setText(AppConfig.spKey);
		} 
		else {
			if(AppConfig.mContext!=null){
				((CustomActivity)AppConfig.mContext).reqSpKey();
			}
		}

		mPopularize.setOnClickListener(mPopularizeListener);
		mBounsRule.setOnClickListener(mRuleListener);
		mGotoWX.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(Util.isEmptyStr(AppConfig.spKey)){
					Toast.makeText(context, "您尚未获取推广码，请检查您的网络后重新分享给好友，谢谢您的支持。",
							Toast.LENGTH_SHORT).show();
					return;
				}
				startActivity(new Intent(context, WXEntryActivity.class));
			}
		});
		btnFreeBean = (ImageView) findViewById(R.id.btnFreeBean);
		btnFreeBean.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ShareActivity.this, MoregameActivity.class);
				startActivity(intent);
				AnalyticsUtils.onClickEvent(ShareActivity.this, UC.EC_224);
			}
		});
		startAnimHander.sendEmptyMessageDelayed(0, 300); //设置延迟启动动画
	}
}
