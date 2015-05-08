package com.mykj.andr.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;

public class NodifyAccountSuccessDialog extends Dialog implements
		android.view.View.OnClickListener {

	TextView tvTop;
	Button btnCancel;
	//Button btnClose;
	TextView tv_show_account;
	TextView tv_show_niname;
	String maccount;
	String mnickName;
	String douCount; // 赠送乐豆信息
	String info; // 推广奖励信息
	Context ctx;

	int theme;

	DialogInterface.OnClickListener callBack;

	public DialogInterface.OnClickListener getCallBack() {
		return callBack;
	}

	public void setCallBack(DialogInterface.OnClickListener callBack) {
		this.callBack = callBack;
	}

	/**
	 * 
	 * <p>
	 * Title:
	 * </p>
	 * <p>
	 * Description:构造函数
	 * </p>
	 * 
	 * @param context
	 *            当前上下文
	 * @param msg
	 *            显示的文本信息
	 * @param theme
	 *            样式，主要是不规则背景图片等
	 */
	public NodifyAccountSuccessDialog(Context context, String account,String nickName, String douCount, String info) {
		super(context, R.style.dialog);
		this.ctx = context;
		this.maccount = account;
		this.mnickName = nickName;
		this.douCount = douCount;
		this.info = info;
	}
	 

	public NodifyAccountSuccessDialog(Context context, int theme,String account,String nickName, String douCount, String info) {
		super(context, theme);
		this.ctx = context;
		this.theme = theme;
		this.maccount = account;
		this.mnickName = nickName;
		this.douCount = douCount;
		this.info = info;
	}

	 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nodufy_success_dialog);
		Resources resource = AppConfig.mContext.getResources();
		tvTop = (TextView)findViewById(R.id.tvTop);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		//btnClose = (Button) findViewById(R.id.exit_btn_cancel);
		btnCancel.setOnClickListener(this);
		//btnClose.setOnClickListener(this);
		tv_show_account = (TextView) findViewById(R.id.tv_show_account);
		tv_show_niname = (TextView) findViewById(R.id.tv_show_niname);
		
		switch (AppConfig.gameId) {
		//case AppConfig.GAMEID_DDZ:
		case AppConfig.GAMEID_WQ:
		case AppConfig.GAMEID_XQ:
			tv_show_account.setTextColor(Color.parseColor("#0b4d77"));
			tv_show_niname.setTextColor(Color.parseColor("#0b4d77"));
			break;
		case AppConfig.GAMEID_WZQ:
			tv_show_account.setTextColor(Color.parseColor("#632c00"));
			tv_show_niname.setTextColor(Color.parseColor("#632c00"));
			break;
		default:
			tv_show_account.setTextColor(Color.WHITE);
			tv_show_niname.setTextColor(Color.WHITE);
			break;
		}
		String tvTopString = resource.getString(R.string.info_modify_success); // 注册成功提示信息
		if(douCount != null && !"0".equals(douCount)){
			tvTopString += "," + resource.getString(R.string.info_obtain) + douCount + resource.getString(R.string.lucky_ledou_2);
		}
		if(null != info && !"null".equals(info)){
			tvTopString += "," + info;
		}
		tvTopString += "!";
		tvTop.setText(tvTopString);
		tv_show_account.setText(maccount);
		tv_show_niname.setText(mnickName);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnCancel || id == R.id.exit_btn_cancel) {
			if (callBack != null) {
				callBack.onClick(NodifyAccountSuccessDialog.this, v.getId());
			}
		}
	}

}
