package com.mykj.andr.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.MyGame.Midlet.R;
import com.mykj.game.utils.Util;


/****
 * @ClassName: CustomDialog
 * @Description: 一般对话框，只包含显示文本，一个动态修改的按钮,默认是确定按钮
 * @author 
 * @date 2012-7-26 下午03:57:15
 *
 */
public class CustomDialog extends Dialog{
	public static int SHOW_SERVER_DIAL = 1;
	public static int HIDE_SERVER_DIAL = 0;
	private Button btnConfirm;
	private Button btnCancel;
	private boolean mShowCancle = false;
	private boolean mShowConfirm = true;
	
	private CheckBox btn_pay_ridio;
	private TextView tvMsg;
	private CharSequence msg;
	private TextView tvServer;         //联系客服说明
	private ImageButton ibServer;      //联系客服按钮
	
	private CharSequence btnConfirmStr = null;
	private CharSequence btnCancelStr = null;
	
	/**是否显示不在提示选项框***/
	private boolean pay_ridio=false;
	
	android.view.View.OnClickListener confirmCallBack;
	android.view.View.OnClickListener cancelCallBack;

	Context mContext;
	private int showServerDail = HIDE_SERVER_DIAL;    
	/**
	 * 
	 * <p>Title:</p>
	 * <p>Description:构造函数</p>
	 * @param context 当前上下文
	 * @param msg     显示的文本信息
	 * @param theme   样式，主要是不规则背景图片等
	 */
	public CustomDialog(Context context,CharSequence msg){
		super(context,  R.style.dialog);
		this.msg=msg;
		mContext = context;
	}

	public CustomDialog(Context context,CharSequence msg,boolean showCancle){
		this(context,  msg);
		mShowCancle=showCancle;
	}
	
	public CustomDialog(Context context,CharSequence msg,boolean showConfirm, boolean showCancle){
		this(context,  msg);
		mShowConfirm = showConfirm;
		mShowCancle=showCancle;
	}
	
	public CustomDialog(Context context,CharSequence msg, int showServerDial){
		this(context, msg);
		this.showServerDail = showServerDial;
	}
	
	public CustomDialog(Context context,CharSequence msg, int showServerDial, CharSequence btnConfirmStr){
		this(context, msg, showServerDial);
		this.btnConfirmStr = btnConfirmStr;
	}
	
	public CustomDialog(Context context,CharSequence msg, int showServerDial, CharSequence btnConfirmStr,CharSequence btnCancelStr){
		this(context, msg, showServerDial, btnConfirmStr);
		this.btnCancelStr=btnCancelStr;
		mShowCancle=true;
	}
	
	public CustomDialog(Context context, CharSequence msg, boolean showCancle, int showServerDial,boolean payridio){
		this(context, msg, showCancle);
		this.showServerDail = showServerDial;
		this.pay_ridio=payridio;
	}
	
	public CustomDialog(Context context, CharSequence msg, boolean showCancle, int showServerDial, CharSequence confirmStr,boolean payridio){
		this(context, msg, showCancle, showServerDial,payridio);
		this.btnConfirmStr = confirmStr;
	}
	
	
	
	public CustomDialog(Context context,CharSequence msg, CharSequence btnConfirmStr){
		this(context, msg);
		this.btnConfirmStr=btnConfirmStr;
	}
	
	
	public CustomDialog(Context context,CharSequence msg, CharSequence btnConfirmStr,CharSequence btnCancelStr){
		this(context,  msg);
		this.btnConfirmStr=btnConfirmStr;
		this.btnCancelStr=btnCancelStr;
		mShowCancle=true;
	}
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prompt_dialog);
		
		btnConfirm=(Button)findViewById(R.id.btnConfir); 
		btnConfirm.setOnClickListener(new ConfirmCallBack());
		if(btnConfirmStr != null){
			btnConfirm.setText(btnConfirmStr);
		}
		if(!mShowConfirm){
			btnConfirm.setVisibility(View.GONE);
		}
		
		btn_pay_ridio= (CheckBox) findViewById(R.id.btn_ridio);
		if(pay_ridio){
			btn_pay_ridio.setVisibility(View.VISIBLE);
		}
		
		
		btnCancel=(Button)findViewById(R.id.btnCancel); 
		btnCancel.setOnClickListener(new CancelCallBack());
		if(btnCancelStr != null){
			btnCancel.setText(btnCancelStr);
		}
		if(!mShowCancle){
			btnCancel.setVisibility(View.GONE);
		}
		
		findViewById(R.id.iv_cancel).setOnClickListener(new CancelCallBack());
		tvMsg=(TextView)findViewById(R.id.tvMsg);
		if(msg != null){
			tvMsg.setText(msg);
		}
		
		tvServer = (TextView)findViewById(R.id.tv_server);
		tvServer.setText(getContext().getResources().getString(R.string.server_description)+ServerDialog.SERVER_PHONE);
		ibServer = (ImageButton)findViewById(R.id.iv_dial);
		View.OnClickListener linsener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String phonenum=ServerDialog.SERVER_PHONE;
				if(!Util.isEmptyStr(phonenum)){
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phonenum));  
					mContext.startActivity(intent); 
				}
			}
		};
		ibServer.setOnClickListener(linsener);
		tvServer.setOnClickListener(linsener);
		if(showServerDail == HIDE_SERVER_DIAL){
			findViewById(R.id.ll_server_dail).setVisibility(View.GONE);
		}
		
	}


	/**
	 * 隐藏取消按钮
	 */
	public void setBtnCancelGone(){
		if(btnCancel!=null){
			btnCancel.setVisibility(View.GONE);
		}
	}
	
	
	public CheckBox getRadio(){
		return btn_pay_ridio;
	}
	
	
	
    public void setConfirmCallBack(android.view.View.OnClickListener confirmCallBack) {
		this.confirmCallBack = confirmCallBack;
	}




	public void setCancelCallBack(android.view.View.OnClickListener cancelCallBack) {
		this.cancelCallBack = cancelCallBack;
	}








	private class ConfirmCallBack implements android.view.View.OnClickListener{
		@Override
		public void onClick(View v) {
			dismiss();
			if(confirmCallBack!=null){
				confirmCallBack.onClick(v);
			}
		}
    }

    
    private class CancelCallBack implements android.view.View.OnClickListener{
		@Override
		public void onClick(View v) {
			dismiss();
			if(cancelCallBack!=null){
				cancelCallBack.onClick(v);
			}
		}
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	if(cancelCallBack!=null){
			cancelCallBack.onClick(new View(mContext));
		}
    }
}
