package com.mykj.andr.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MyGame.Migu.R;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

/**
 * 
 * @ClassName: PromptDialog
 * @Description: 提示对话框
 * @author Administrator
 * @date 2012-7-25 上午09:49:06
 * 
 */
public class PromptDialog extends AlertDialog implements
		android.view.View.OnClickListener {
	public static final int PROMPT_DIALOG_DRAWABLE_INVALID = -1;
	public static final int PROMPT_DIALOG_THEME_DEFAULT = 0;
	private Button btnConfir;
	private Button btnCancel;
	private boolean isShowCancel = true;
	private LinearLayout lLeft = null;
	private TextView tvMsg;
	private String msg;
	private Context ctx;
	private int mDrawableID;
	private int theme;
	private SpannableString mSpannable;

	private DialogInterface.OnClickListener callBack;
	

	public DialogInterface.OnClickListener getCallBack() {
		return callBack;
	}

	public void setCallBack(DialogInterface.OnClickListener callBack) {
		this.callBack = callBack;
	}

	public PromptDialog(Context context) {
		this(context, "", PROMPT_DIALOG_THEME_DEFAULT, PROMPT_DIALOG_DRAWABLE_INVALID);
	}

	public PromptDialog(Context context, String msg) {
		this(context, msg, PROMPT_DIALOG_THEME_DEFAULT, PROMPT_DIALOG_DRAWABLE_INVALID);
	}

	public PromptDialog(Context context, String msg, int drawableID) {
		this(context, msg, PROMPT_DIALOG_THEME_DEFAULT, drawableID);
	}
	
	public PromptDialog(Context context, String msg, int drawableID, boolean showCancel){
		this(context, msg, PROMPT_DIALOG_THEME_DEFAULT);
		isShowCancel = showCancel;
	}
	
	public PromptDialog(Context context, String msg, int theme, int drawableID) {
		this(context, msg, theme, drawableID, true);
	}
	
	public PromptDialog(Context context, String msg, int theme, int drawableID, boolean showCancel){
		super(context, theme);
		this.ctx = context;
		this.msg = msg;
		this.mDrawableID = drawableID;
		this.theme = theme;
		this.isShowCancel = showCancel;
	}
	
	public void hideCancel(){
		isShowCancel = false;
		if(lLeft != null){
			lLeft.setVisibility(View.GONE);
		}
	}
	
	public void showCancel(){
		isShowCancel = true;
		if(lLeft != null){
			lLeft.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 构造方法，初始化UI颜色，以及自动以文本颜色
	 * <p>Title:</p>
	 * <p>Description:</p>
	 * @param context
	 * @param drawableID
	 * @param spannable
	 */
	public PromptDialog(Context context, int drawableID,
			SpannableString spannable) {
		this(context, "", PROMPT_DIALOG_THEME_DEFAULT, drawableID);
		this.mSpannable = spannable;
	}

	public PromptDialog(Context context, int theme) {
		this(context, "", theme, PROMPT_DIALOG_DRAWABLE_INVALID);
	}

	public void setSpannableString(SpannableString spannable){
		this.mSpannable = spannable;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prompt_dialog);
		btnConfir = (Button) findViewById(R.id.btnConfir);
		if (mDrawableID != PROMPT_DIALOG_DRAWABLE_INVALID && mDrawableID != PROMPT_DIALOG_THEME_DEFAULT)
			btnConfir.setBackgroundResource(mDrawableID);

		btnConfir.setOnClickListener(this);
		
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);
		
		tvMsg = (TextView) findViewById(R.id.tvMsg);
		tvMsg.setTextColor(Color.WHITE);
		
		if (msg!=null && msg.length() > 0){
			msg = Util.toDBC(msg);
			tvMsg.setText(msg);
		}
		else if (mSpannable != null) { // 设置某一字高亮
			msg = Util.toDBC(mSpannable.toString());
			tvMsg.setText(mSpannable);
		}
		lLeft = (LinearLayout) findViewById(R.id.system_pop_ll_left);
		if(isShowCancel){
			lLeft.setVisibility(View.VISIBLE);
		}else{
			lLeft.setVisibility(View.GONE);
		}
	}

	public PromptDialog create() {
		if (mDrawableID != PROMPT_DIALOG_DRAWABLE_INVALID && mDrawableID != 0)
			btnConfir.setBackgroundResource(mDrawableID);
		return this;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnConfir) {
			if (callBack != null) {
				callBack.onClick(PromptDialog.this, 0);
			}
		}else if(id == R.id.btnCancel){
			dismiss();// 退出
			if (UtilHelper.getTopActName(ctx).equals("com.mykj.andr.ui.SuperDialogActivity")) {
				((Activity) ctx).finish();
			}
		}

	}
}
