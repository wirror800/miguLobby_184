package com.mykj.andr.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;

/**
 * 
 * @ClassName: PromptDialog
 * @Description: 提示对话框
 * @author Administrator
 * @date 2012-7-25 上午09:49:06
 * 
 */
public class SysPopDialog extends AlertDialog implements
		android.view.View.OnClickListener {
	private static final String Tag = "SysPopDialog";
	private Button closeDlg;
	private Button confirmBtn;
	private Button cancelBtn;
	private TextView tvMsg;
	private String msg;

	public static final int CONFIRM = 1;

	public static final int CANCEL = 0;

	public static final int CLOSE = 2;
	
	private SpannableString mSpannable;

	private DialogInterface.OnClickListener callBack;

	private String confirmStr;
	private String cancelStr;
	private String titleStr;
	private boolean hasCloseBtn = true;
	public SysPopDialog(Context context, String confirmStr, String cancelStr,
			String msg, DialogInterface.OnClickListener listen) {
		/*super(context, R.style.dialog);
		this.msg = msg;
		this.confirmStr = confirmStr;
		this.cancelStr = cancelStr;
		callBack = listen;*/
		this(context, confirmStr, cancelStr, msg, null, listen, true);
	}
	public SysPopDialog(Context context, String confirmStr, String cancelStr,
			String msg, String titlestr, DialogInterface.OnClickListener listen, boolean hasXBtn) {
		super(context, R.style.dialog);
		this.msg = msg;
		this.confirmStr = confirmStr;
		this.cancelStr = cancelStr;
		this.titleStr = titlestr;
		callBack = listen;
		this.hasCloseBtn = hasXBtn;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.system_pop_dialog);
		setContentView(R.layout.force_notice_dialog);
		closeDlg = (Button) findViewById(R.id.exit_btn_cancel);
		closeDlg.setOnClickListener(this);
		if(!hasCloseBtn){
			closeDlg.setVisibility(View.GONE);
		}
		confirmBtn = (Button) findViewById(R.id.confirmBtn);
		//confirmBtn.getPaint().setFakeBoldText(true);
		cancelBtn = (Button) findViewById(R.id.cancelBtn);
		//cancelBtn.getPaint().setFakeBoldText(true);
		confirmBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);

		TextView title = (TextView)findViewById(R.id.tvTitle);
		if(titleStr != null && !titleStr.trim().equals("")){
			title.setText(titleStr);
		}
		if (confirmStr != null && !confirmStr.trim().equals("")) {
			//confirmBtn.setVisibility(View.VISIBLE);
			confirmBtn.setText(confirmStr);
		}else{
			findViewById(R.id.system_pop_ll_left).setVisibility(View.GONE);
		}
		if (cancelStr != null && !cancelStr.trim().equals("")) {
			//cancelBtn.setVisibility(View.VISIBLE);
			cancelBtn.setText(cancelStr);
		}else{
			findViewById(R.id.system_pop_ll_right).setVisibility(View.GONE);
		}
		tvMsg = (TextView) findViewById(R.id.tvTop);
		TextView label = (TextView)findViewById(R.id.tvLabel);
		if (msg != null && msg.length() > 0){
			int div = msg.indexOf("*#*");
			if(div > 0 && div < msg.length()){
				String temp1 = msg.substring(0, div);
				String temp2 = msg.substring(div + 3);
				label.setVisibility(View.VISIBLE);
				label.setText(temp1);
				tvMsg.setText(temp2);
				title.setText(AppConfig.mContext.getResources().getString(R.string.notice));
			}else{
				tvMsg.setText(msg);
			}
		}
		else if (mSpannable != null) { // 设置某一字高亮
			tvMsg.setText(mSpannable);
		}
		
		ImageView jumpImg = (ImageView) findViewById(R.id.iv_dlg_pump);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -20);
		animation.setDuration(300);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);
		jumpImg.startAnimation(animation);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.confirmBtn) {
			if (confirmBtn != null && callBack != null) {
				callBack.onClick(this, CONFIRM);
			}
		} else if (id == R.id.cancelBtn) {
			if (cancelBtn != null && callBack != null) {
				callBack.onClick(this, CANCEL);
			}
		} else if (id == R.id.exit_btn_cancel){
			if(closeDlg != null && callBack != null){
				callBack.onClick(this, CLOSE);
			}
		}
		dismiss();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(callBack != null){
			callBack.onClick(this, CLOSE);
		}
	}
	
	@Override
	public void show() {
		
		/**当activity destroy后show会报异常，异步可能出现问题*/
		try{
			super.show();
		}catch(Exception e){
			Log.e(Tag,"show exception: " + e.getMessage());
		}
	}

}
