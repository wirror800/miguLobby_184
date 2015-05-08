package com.mykj.game.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.moregame.MoregameActivity;

/**
 * 交叉推广相关函数
 * 
 */
public class CrossGeneralizeHelper {

	/** 提醒用户游戏累的时间 */
	public static final int WARMUSER_TIRED_TIME = 20;


	private static CrossGeneralizeHelper instance;

	private CrossGeneralizeHelper() {
	}

	public static CrossGeneralizeHelper getInstance() {
		if (instance == null)
			instance = new CrossGeneralizeHelper();
		return instance;
	}

	/** 是否提醒过用户游戏太累 */
	private boolean hasWarmTired = false;
	/** 记录进入游戏的时间 */
	private long intoGameTime;

	/**
	 * 设置进入游戏的时间
	 * 
	 * @param time
	 */
	public void markIntoGameTime() {
		if ( isOpenInGame()&&!hasWarmTired) {
			intoGameTime = SystemClock.elapsedRealtime();
		}
	}

	/**
	 * 处理是否弹出用户太累提醒
	 */
	public void handleUserTiredWarm(Context c) {
		if (isOpenInGame() && needWarmUserTired()) {
			showDialog(c);
		}
	}

	/**
	 * 是否需要 提醒用户
	 * 
	 * @param currentTime
	 * @return
	 */
	private boolean needWarmUserTired() {
		if (!hasWarmTired) {
			long min = (SystemClock.elapsedRealtime() - intoGameTime) / 1000 / 60;
			if (min >= WARMUSER_TIRED_TIME) {
				return true;
			}
		}
		return false;
	}

	private void showDialog(Context context) {
		WarmTiredDialog dialog = new WarmTiredDialog(context);
		dialog.show();
		setHasWarmTired();
	}

	/**第30位
	 * 中途交叉推广开关,
	 * 是否提醒用户太累，弹出交叉推广对话框
	 * @return
	 */
	private static boolean isOpenInGame() {
		int statusBit = FiexedViewHelper.getInstance().getUserStatusBit();
		int flagBit = 1<<29;//第30位
		if((statusBit & flagBit)!=0){
			return true;
		}
		return false;
	}
	
	/**第29位
	 * 退出对话框中是否弹出交叉推广框
	 * @return
	 */
	public static boolean isOpenInExitDialog(){
		int statusBit = FiexedViewHelper.getInstance().getUserStatusBit();
		int flagBit = 1<<28;//第29位
		if((statusBit & flagBit)!=0){
			return true;
		}
		return false;
	}
	
	

	private void setHasWarmTired() {
		hasWarmTired = true;
	}

	class WarmTiredDialog extends Dialog implements
			android.view.View.OnClickListener {

		public WarmTiredDialog(Context context) {
			super(context, R.style.dialog);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.warm_tired_dialog);
			init();
		}

		private void init() {
			findViewById(R.id.btn_sure).setOnClickListener(this);
			;
			findViewById(R.id.btn_cancel).setOnClickListener(this);
			TextView tv = (TextView) findViewById(R.id.tv_warm);

			SpannableStringBuilder ssb = new SpannableStringBuilder(
					"玩了那么久，换个游戏轻松一下，还有");

			ImageSpan imageSpan = new ImageSpan(getContext(),
					R.drawable.icon_bean);
			SpannableString spannableString = new SpannableString(" ");
			spannableString.setSpan(imageSpan, spannableString.length() - 1,
					spannableString.length(),
					Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			ssb.append(spannableString).append("送哦！");
			tv.setText(ssb);
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_sure:
				//交叉推广，记录渠道号
				ChannelDataMgr.getInstance().writeChannelToSDCard();

				//wanghj 2013-04-16 跳转到更多游戏界面，不是wap网页
				Intent intent = new Intent(getContext(), MoregameActivity.class);
				getContext().startActivity(intent);
				dismiss();
				break;
			case R.id.btn_cancel:
				dismiss();
				break;

			default:
				break;
			}
		}

	}

}
