package com.mykj.andr.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mingyou.accountInfo.AccountItem;
import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.UtilHelper;

public class ModifyPasswordActivity extends Activity implements
		OnClickListener, RadioGroup.OnCheckedChangeListener, OnFocusChangeListener {

	//private final static String TAG = "ModifyPasswordActivity";
	
	protected Button mSaveBtn, mModifyBtn;

	protected RadioGroup mRadioGroup;
	protected RadioButton btnMobile, btnNotMobile;

	protected EditText et1, et2, et3;

	protected ImageView mIcon1, mIcon2, mIcon3;
	protected TextView mText1, mText2, mText3;

	protected TextView mInfo1, mInfo2, mInfo3;
	protected ImageView mError1, mError2, mError3;
	protected ImageView mRight1, mRight2, mRight3;

	private String et1Str, et2Str, et3Str;

	private String password = "123456";
	
	
	/** 用户Token */
	public static final String USER_TOKEN = "user_token";
	
	public String userToken = "";
	
	private static final short LS_TRANSIT_LOGON = 18;
	/** 子协议-用户密码修改请求 */
	private static final short MSUB_CMD_MODIFY_PWD_REQ =20;
	/** 子协议-用户密码修改结果 */
	private static final short MSUB_CMD_MODIFY_PWD_RESP =21;
	
	public static final int HANDLER_MODIFY_SUCCESS = 40;

	public static final int HANDLER_MODIFY_FAIL = 50;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_activity);

		userToken=HallDataManager.getInstance().getUserMe().Token;
		
		
		
		init();
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
	private void init() {
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.changepassword);
		findViewById(R.id.tvBack).setOnClickListener(this);
		mModifyBtn = (Button) findViewById(R.id.forget_password);
		mSaveBtn = (Button) findViewById(R.id.mini_save);
		mRadioGroup = (RadioGroup) findViewById(R.id.modify_radio);
		btnMobile = (RadioButton) findViewById(R.id.mobile_user);
		btnNotMobile = (RadioButton) findViewById(R.id.not_mobile_user);

		mIcon1 = (ImageView) findViewById(R.id.iconId);
		mIcon2 = (ImageView) findViewById(R.id.iconCard);
		mIcon3 = (ImageView) findViewById(R.id.iconPhoneNum);
		mText1 = (TextView) findViewById(R.id.password_label1);
		mText2 = (TextView) findViewById(R.id.password_label2);
		mText3 = (TextView) findViewById(R.id.password_label3);
		mInfo1 = (TextView) findViewById(R.id.password_error1);
		mInfo2 = (TextView) findViewById(R.id.password_error2);
		mInfo3 = (TextView) findViewById(R.id.password_error3);

		mError1 = (ImageView) findViewById(R.id.error1);
		mError2 = (ImageView) findViewById(R.id.error2);
		mError3 = (ImageView) findViewById(R.id.error3);
		mRight1 = (ImageView) findViewById(R.id.right1);
		mRight2 = (ImageView) findViewById(R.id.right2);
		mRight3 = (ImageView) findViewById(R.id.right3);

		et1 = (EditText) findViewById(R.id.et1_password);
		et2 = (EditText) findViewById(R.id.et2_password);
		et3 = (EditText) findViewById(R.id.et3_password);

		et1.setOnFocusChangeListener(this);
		et2.setOnFocusChangeListener(this);
		et3.setOnFocusChangeListener(this);
		
		et3.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				updateInfo3();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
//				updateInfo3();
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		}); 
		mModifyBtn.setOnClickListener(this);
		mSaveBtn.setOnClickListener(this);
		mRadioGroup.setOnCheckedChangeListener(this);

		btnMobile.setChecked(true);
		mobileUser();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tvBack) {
			finish();
		}else if(id == R.id.forget_password){
			UtilHelper.onWeb(ModifyPasswordActivity.this, AppConfig.MODIFY_URL);
			//startWebViewActivity(modifyUrl);
		}else if(id == R.id.mini_save){
			String oldPassword = et1.getText().toString();
			et2Str = et2.getText().toString();
			String newPassword = et3.getText().toString();
			if ((!et2Str.equals("")) && et2Str.equals(newPassword)) {
				modifyPassword(newPassword, oldPassword);
			} else {
				Toast.makeText(getApplication(), ModifyPasswordActivity.this.getResources().getString(R.string.info_repassword_error), Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		int id = v.getId();
		if (id == R.id.et2_password) {
			updateInfo2();
		}else if(id == R.id.et3_password){
			updateInfo3();
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int id = group.getId();
		if (id == R.id.modify_radio) {
			if (checkedId == btnMobile.getId()) {
				mobileUser();
			} else if (checkedId == btnNotMobile.getId()) {
				notMobileUser();
			}
		}

	}


	
	public void notMobileUser() {
		enabled();
		et1.setEnabled(true);
		et2.setEnabled(true);
		et3.setEnabled(true);
		/*mSaveBtn.setEnabled(true);
		mModifyBtn.setEnabled(false);*/
		
		mSaveBtn.setClickable(true);
		mModifyBtn.setClickable(false);
	}

	public void mobileUser() {
		disabled();
		et1.setEnabled(false);
		et2.setEnabled(false);
		et3.setEnabled(false);
		/*mSaveBtn.setEnabled(false);
		mModifyBtn.setEnabled(true);*/
		mSaveBtn.setClickable(false);
		mModifyBtn.setClickable(true);
	}

	public void enabled() {
		mIcon1.setAlpha(255);
		mIcon2.setAlpha(255);
		mIcon3.setAlpha(255);
		mText1.setTextColor(Color.argb(255, 255, 255, 255));
		mText2.setTextColor(Color.argb(255, 255, 255, 255));
		mText3.setTextColor(Color.argb(255, 255, 255, 255));
		et1.setBackgroundColor(Color.argb(255, 255, 255, 255));
		et2.setBackgroundColor(Color.argb(255, 255, 255, 255));
		et3.setBackgroundColor(Color.argb(255, 255, 255, 255));
	}
	
	public void disabled() {
		mIcon1.setAlpha(128);
		mIcon2.setAlpha(128);
		mIcon3.setAlpha(128);
		mText1.setTextColor(Color.argb(128, 255, 255, 255));
		mText2.setTextColor(Color.argb(128, 255, 255, 255));
		mText3.setTextColor(Color.argb(128, 255, 255, 255));
		et1.setBackgroundColor(Color.argb(128, 255, 255, 255));
		et2.setBackgroundColor(Color.argb(128, 255, 255, 255));
		et3.setBackgroundColor(Color.argb(128, 255, 255, 255));	
	}

	public void updateInfo1() {
		et1Str = et1.getText().toString().trim();
		if (et1Str.equals("")){
			mInfo1.setVisibility(View.INVISIBLE);
			mError1.setVisibility(View.GONE);
			mRight1.setVisibility(View.GONE);
		} else if (!password.equals(et1Str)) {
			mInfo1.setVisibility(View.VISIBLE);
			mError1.setVisibility(View.VISIBLE);
			mRight1.setVisibility(View.GONE);
		} else if (password.equals(et1Str)) {
			mInfo1.setVisibility(View.INVISIBLE);
			mError1.setVisibility(View.GONE);
			mRight1.setVisibility(View.VISIBLE);
		} 
	}

	public void updateInfo2() {
		et2Str = et2.getText().toString().trim();
		if (et2Str.equals("")){
			mInfo2.setVisibility(View.INVISIBLE);
			mError2.setVisibility(View.GONE);
			mRight2.setVisibility(View.GONE);
		} else if (et2Str.length() < 6) {
			mInfo2.setVisibility(View.VISIBLE);
			mError2.setVisibility(View.VISIBLE);
			mRight2.setVisibility(View.GONE);
		} else if (et2Str.length() >= 6) {
			mInfo2.setVisibility(View.INVISIBLE);
			mError2.setVisibility(View.GONE);
			mRight2.setVisibility(View.VISIBLE);
		} 
	}

	public void updateInfo3() {
		et2Str = et2.getText().toString().trim();
		et3Str = et3.getText().toString().trim();
		if (!et2Str.equals("") && !et3Str.equals("")) {
			if (!et2Str.equals(et3Str)) {
				mInfo3.setVisibility(View.VISIBLE);
				mError3.setVisibility(View.VISIBLE);
				mRight3.setVisibility(View.GONE);
			} else if (et2Str.equals(et3Str)) {
				mInfo3.setVisibility(View.INVISIBLE);
				mError3.setVisibility(View.GONE);
				mRight3.setVisibility(View.VISIBLE);
			} 
		} else {
			mInfo3.setVisibility(View.INVISIBLE);
			mError3.setVisibility(View.GONE);
			mRight3.setVisibility(View.GONE);
		}
	}
	
	public void updateModifySuccess() {
		et1.setText("");
		et2.setText("");
		et3.setText("");
		mInfo1.setVisibility(View.INVISIBLE);
		mInfo2.setVisibility(View.INVISIBLE);
		mInfo3.setVisibility(View.INVISIBLE);
		mError1.setVisibility(View.GONE);
		mRight1.setVisibility(View.GONE);
		mError2.setVisibility(View.GONE);
		mRight2.setVisibility(View.GONE);
		mError3.setVisibility(View.GONE);
		mRight3.setVisibility(View.GONE);
		
	}

	String buildPassword(String at, String newPassword, String oldPassword){
		
		StringBuffer sb = new StringBuffer();
		sb.append("<m>").append("<p n=\"at\" v=\"");
		sb.append(at);
		sb.append("\"/>").append("<p n=\"password\" v=\"");
		sb.append(newPassword);
		sb.append("\"/>").append("<p n=\"oldpass\" v=\"");
		sb.append(oldPassword);
		sb.append("\"/>").append("</m>");
		return sb.toString();
	}

	private void modifyPassword(final String newPassword, String oldPassword){
		//创建发送的数据包
		
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.writeUTF(buildPassword(userToken, newPassword, oldPassword));
		
		NetSocketPak passwordInfo = new NetSocketPak(LS_TRANSIT_LOGON, MSUB_CMD_MODIFY_PWD_REQ, tdos);
		//定义接受数据的协议
		short [][] parseProtocol = {{LS_TRANSIT_LOGON, MSUB_CMD_MODIFY_PWD_RESP }};
		//创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				//解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					byte[] bdata = new byte[tdis.available()];
					tdis.read(bdata);
					String dataStr = new String(bdata,"UTF-8");


					String status = "";
					String statusnote = "";		

					status = UtilHelper.parseStatusXml(dataStr, "status");						
					statusnote = UtilHelper.parseStatusXml(dataStr, "statusnote");

//					if (status.equals("0") && statusnote.equals("修改成功")) {
					if (status.equals("0")) {
						//Do something
						Message msg=handler.obtainMessage(HANDLER_MODIFY_SUCCESS);
						Bundle data=new Bundle();
						data.putString("newpassword", newPassword);
						data.putString("statusnote", statusnote);
						msg.setData(data);
						handler.sendMessage(msg);
						
						//handler.sendMessage(handler.obtainMessage(HANDLER_MODIFY_SUCCESS, statusnote));
					} else {
						handler.sendMessage(handler.obtainMessage(HANDLER_MODIFY_FAIL, statusnote));
						
					}
				} catch (Exception e) {
					e.printStackTrace();
					
				}
				//数据处理完成，终止继续解析
				return true;
			}
		};
		//注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		//发送协议
		NetSocketManager.getInstance().sendData(passwordInfo);
		//清理协议对象
		passwordInfo.free();
	}

	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case HANDLER_MODIFY_SUCCESS:
				Bundle data=msg.getData(); 
				UserInfo userInfo = HallDataManager.getInstance().getUserMe();
				AccountItem ai = new AccountItem(userInfo.account, data.getString("newpassword"), userInfo.Token, userInfo.loginType, userInfo.userID);
				LoginInfoManager.getInstance().updateAccPassInfo(ai);
				Toast.makeText(getApplication(), data.getString("statusnote"), Toast.LENGTH_SHORT).show();
				finish();
				break;
				
			case HANDLER_MODIFY_FAIL: // 接受错误数据
				
				Toast.makeText(getApplication(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
				
				break;
			}
		}
	};
	
}
