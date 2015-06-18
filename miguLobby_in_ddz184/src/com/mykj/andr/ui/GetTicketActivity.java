package com.mykj.andr.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.DateDetailInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.widget.SysPopDialog;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.MyGame.Midlet.wxapi.WXEntryActivity;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class GetTicketActivity extends Activity implements OnClickListener{
	private static final  String TAG = "GetTicketActivity";

	public  static final int TICKET_CHANGED = 256;

	public  static final int HUAFEI_QUAN=403;  //话费券ID


	/***********************协议码**************************************/
	private static final short MDM_PROP = 17;
	private static final short LS_TRANSIT_LOGON = 18;
	/** 子协议-获取用户信息请求 */
	private static final short MSUB_CMD_USERINFO_EX_REQ = 22;
	/** 子协议-获取用户信息结果 */
	private static final short MSUB_CMD_USERINFO_EX_RESP = 23;

	/** 子协议-新道具使用请求 */
	private static final short MSUB_CMD_USE_PROP_REQ = 780;
	/** 子协议-新道具使用返回 */
	private static final short MSUB_CMD_USE_PROP_RESP = 781;
	/***********************协议码**************************************/



	/***********************handler what******************************/
	private static final int  HANDLER_HUAFEIQUAN_SUCCESS=0;
	private static final int  HANDLER_REQTICKET_SUCCESS=1; // 接受成功数据
	private static final int  HANDLER_REQTICKET_FAIL=2; // 接受错误数据
	/***********************handler what******************************/


	private int userTicket;
	private int propId = HUAFEI_QUAN;
	private int ticketCount;   //默认选取30


	private TextView tvBack;
	private TextView tvCall;  //客服电话
	private TextView tvGetTicket;
	private RadioGroup mRadioGroup;
	private RadioButton btnThirty;
	private EditText etPhoneNum, etPhoneNumEnsure;
	private TextView tvTicketValue, mUrlText;
	private TextView tvNum, tvNumEnsure;
	private TextView tvPhoneNumErr, tvPhoneNumEnsureErr;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_ticket_activity);
		init();
		refreshTicket();
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
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.getticket);
		tvBack = (TextView) findViewById(R.id.tvBack);
		tvBack.setOnClickListener(this);

		tvCall = (TextView) findViewById(R.id.tvCall);
		tvCall.setOnClickListener(this);

		tvGetTicket = (TextView) findViewById(R.id.get_ticket);
		tvGetTicket.setOnClickListener(this);

		etPhoneNum = (EditText) findViewById(R.id.et_mobile);
		etPhoneNum.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				updatePhoneInput();
				updatePhoneInputEnsure();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		etPhoneNumEnsure = (EditText) findViewById(R.id.et_ensure_mobile);
		etPhoneNumEnsure.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				updatePhoneInputEnsure();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});



		tvPhoneNumErr = (TextView) findViewById(R.id.mobile_info);
		tvPhoneNumEnsureErr = (TextView) findViewById(R.id.ensure_mobile_info);

		tvNum = (TextView) findViewById(R.id.mobile_label);
		tvNumEnsure = (TextView) findViewById(R.id.ensure_mobile_label);

		tvTicketValue = (TextView) findViewById(R.id.tvTicketValue);
		mUrlText = (TextView) findViewById(R.id.agree_web);

		mUrlText.setOnClickListener(this);
		mUrlText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

		btnThirty = (RadioButton) findViewById(R.id.thirty_ticket);

		mRadioGroup = (RadioGroup) findViewById(R.id.balance_radio);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.thirty_ticket){
					ticketCount = 30;
				}else if(checkedId==R.id.fifty_ticket){
					ticketCount = 50;
				}else if(checkedId==R.id.hundred_ticket){
					ticketCount = 100;
				}
				if (userTicket < ticketCount) {
					disabled();
				} else {
					enabled();
				}
			}
		});

		btnThirty.setChecked(true);  //默认选取30

	}



	@Override
	public void onClick(View v) {
		int id=v.getId();
		if(id==R.id.agree_web){
			UtilHelper.onWeb(GetTicketActivity.this, AppConfig.HUAFEI_INFO);
		}else if(id==R.id.tvBack){
			Intent intent = getIntent();
			Bundle bundle = new Bundle();
			bundle.putInt("ticket", userTicket);
			intent.putExtras(bundle);
			setResult(TICKET_CHANGED, intent);
			finish();
		}else if(id==R.id.tvCall){
//			String phonenum=ServerDialog.SERVER_PHONE;
//			if(!Util.isEmptyStr(phonenum)){
//				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phonenum));  
//				startActivity(intent); 
//			}
			ServerDialog dialog= new ServerDialog(this);
			dialog.show();
			
		}else if(id==R.id.get_ticket){
			String phoneNumStr = etPhoneNum.getText().toString().trim();
			String phoneNumStrEnsure = etPhoneNumEnsure.getText().toString().trim();

			if ((!Util.isEmptyStr(phoneNumStrEnsure))
					&& (Pattern.compile("^1[3458]\\d{9}$").matcher(phoneNumStrEnsure).matches())
					&& (phoneNumStrEnsure.equals(phoneNumStr))
					&& (ticketCount != 0)) {
				int userId=FiexedViewHelper.getInstance().getUserId();
				sendTicketRequst(userId, propId, 0, 0, 0, (byte) 2,
						buildExtXml(phoneNumStrEnsure, ticketCount));

			} 

		}
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = getIntent();
			Bundle bundle = new Bundle();
			bundle.putInt("ticket", userTicket);
			intent.putExtras(bundle);

			setResult(TICKET_CHANGED, intent);

			this.finish();
			return true;
		}

		return false;
	}





	@SuppressLint("HandlerLeak")
	public Handler mTicketHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_HUAFEIQUAN_SUCCESS:
				tvTicketValue.setText(userTicket + getString(R.string.market_yuan));
//				if (userTicket < 30) {
//					String str=getString(R.string.tick_count)+userTicket+getString(R.string.market_yuan);
//					UtilHelper.showCustomDialog(GetTicketActivity.this, str+getString(R.string.tick_no_enough));
//				} 
				if (userTicket < ticketCount) {
					disabled();
				} else {
					enabled();
				}
				break;
			case HANDLER_REQTICKET_SUCCESS: // 接受成功数据
				Resources resource = GetTicketActivity.this.getResources();
				SysPopDialog dialog = new SysPopDialog(GetTicketActivity.this, resource.getString(R.string.ddz_shared), resource.getString(R.string.Ensure),
						msg.obj.toString(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						if (which == SysPopDialog.CONFIRM) {
							startActivity(new Intent(GetTicketActivity.this, WXEntryActivity.class));
						}
					}
				});
				dialog.show();
				refreshTicket();
				break;

			case HANDLER_REQTICKET_FAIL: // 接受错误数据
				if (msg.obj != null) {
					Toast.makeText(getApplication(), msg.obj.toString(),
							Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
		}
	};




	/**
	 * 话费券领取协议
	 * @param userId
	 * @param propId
	 * @param indexID
	 * @param exData
	 * @param cliSec
	 * @param cbType
	 * @param extXml
	 */
	private void sendTicketRequst(int userId, int propId, long indexID,
			int exData, long cliSec, final byte cbType, String extXml) {
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userId);
		tdos.writeInt(propId);
		tdos.writeLong(indexID);
		tdos.writeInt(exData);
		tdos.writeLong(cliSec);
		if (cbType != 0){
			tdos.writeByte(cbType);
			tdos.writeUTFShort(extXml);
		}

		NetSocketPak ticketInfo = new NetSocketPak(MDM_PROP,
				MSUB_CMD_USE_PROP_REQ, tdos);

		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_USE_PROP_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.readInt();   //用户ID
					tdis.readInt();   //道具ID
					int result = tdis.readInt();  
					/*
					 * 返回结果状态 
					 *  0-成功，
					 *  32-未找到该道具
					 *  37-您已晋级,不能使用复活卷
					 *  38-用户未晋级,不能使用补血卷
					 *  36-道具使用失败
					 *  4 -参数不完整
					 *  80 -为用户没有此道具提示购买的
					 *  81 -使用失败，需要提供扩展数据
					 */
					tdis.readLong();  // 道具索引编号
					byte len=tdis.readByte();  //消息长度（如失败原因，成功也可能带消息），若改值 >0 说明有消息体，需要客户端显示
					String msgStr = tdis.readUTF(len);
					Message msg=mTicketHandler.obtainMessage();
					msg.obj=msgStr;
					if (result == 0) {
						msg.what=HANDLER_REQTICKET_SUCCESS;
						mTicketHandler.sendMessage(msg);
					} else if (result == 81) {
						Log.e(TAG, "话费券使用失败，需要提供扩展数据");
					}else{
						msg.what=HANDLER_REQTICKET_FAIL;
						mTicketHandler.sendMessage(msg);
					}

					//                  byte type=-1;
					//					if (tdis.available() > 8) {//客户端标识，为上行数据继续返回，如果无上行数据，客户端直接返回
					//						tdis.readLong();
					//						type=tdis.readByte();
					//						/*type
					//						 * 0-无需做任何操作
					//                         * 1为充值卡使用要提示输入数据
					//                         * 2为话费券使用要提示输入数据
					//                         * 3为实物道具使用提示输入数据
					//						 * */
					//					}
					//					short extlen=0;
					//					if(type!=0){
					//						extlen=tdis.readShort();
					//					}
					//					if(extlen>0){// 使用失败，需要提供扩展数据
					//						tdis.readShort();
					//						boolean isBind=tdis.readByte()==0 ? false : true;
					//						int jBPropID = tdis.readInt();
					//						String bindTel=tdis.readUTFByte();
					//
					//					}

				} catch (Exception e) {
					Log.e(TAG, "网络解析数据出现Error");
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(ticketInfo);
		// 清理协议对象
		ticketInfo.free();
	}




	/**
	 * 刷新话费券
	 */
	private void refreshTicket() {
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		String request = UtilHelper.buildInfoHuafei();
		tdos.setFront(false);
		tdos.writeUTF(request);
		NetSocketPak netSocketPak = new NetSocketPak(LS_TRANSIT_LOGON, MSUB_CMD_USERINFO_EX_REQ,
				tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = {{ LS_TRANSIT_LOGON, MSUB_CMD_USERINFO_EX_RESP }};
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				try {
					String ticket = null;
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					// 解析接受到的网络数据
					byte[] bdata = new byte[tdis.available()];
					tdis.read(bdata);
					String data = new String(bdata, "UTF-8");
					String status = "";
					status = UtilHelper.parseStatusXml(data, "status");
					if (status.equals("0")) {
						ticket = UtilHelper.parseStatusXml(data, "mobilevoucher");
						userTicket = Integer.parseInt(ticket);
						mTicketHandler.sendMessage(mTicketHandler.obtainMessage(HANDLER_HUAFEIQUAN_SUCCESS,
								ticket));
						DateDetailInfo.isDateDetailRefresh = true;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);

		// 发送协议
		NetSocketManager.getInstance().sendData(netSocketPak);
		// 清理协议对象
		netSocketPak.free();
	}



	/**
	 * 更新手机号码输入错误提示
	 */
	private void updatePhoneInput() {
		String phoneNumStr = etPhoneNum.getText().toString().trim();
		if (Util.isEmptyStr(phoneNumStr)) {
			tvPhoneNumErr.setVisibility(View.INVISIBLE);
		} else if (!Pattern.compile("^1[3458]\\d{9}$").matcher(phoneNumStr).matches()) {
			setPhoneNumInputError();
		} else if (Pattern.compile("^1[3458]\\d{9}$").matcher(phoneNumStr).matches()) {
			setPhoneNumInputRight();
		}
	}


	/**
	 * 更新手机号码再次输入错误提示
	 */
	private void updatePhoneInputEnsure() {
		String phoneNumStr = etPhoneNum.getText().toString().trim();
		String phoneNumEnsure = etPhoneNumEnsure.getText().toString().trim();

		if (!Util.isEmptyStr(phoneNumEnsure) && !Util.isEmptyStr(phoneNumStr)) {
			if (!phoneNumEnsure.equals(phoneNumStr)) {
				setPhoneNumInputEnsureError();
			} else{
				setPhoneNumInputEnsureRight();
			}
		} else {
			tvPhoneNumEnsureErr.setVisibility(View.INVISIBLE);
		}
	}




	/**
	 * 输入号码提示正确
	 */
	private void setPhoneNumInputRight(){
		if(tvPhoneNumErr.getVisibility()==View.INVISIBLE){
			tvPhoneNumErr.setVisibility(View.VISIBLE);
		}
		tvPhoneNumErr.setText("");
		tvPhoneNumErr.setCompoundDrawablesWithIntrinsicBounds(R.drawable.right,0,0,0);
	}

	/**
	 * 输入号码提示错误
	 */
	private void setPhoneNumInputError(){
		if(tvPhoneNumErr.getVisibility()==View.INVISIBLE){
			tvPhoneNumErr.setVisibility(View.VISIBLE);
		}
		tvPhoneNumErr.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error,0,0,0);
		tvPhoneNumErr.setText(R.string.action_input_phone_number_error);

	}




	/**
	 * 再次输入提示正确
	 */
	private void setPhoneNumInputEnsureRight(){
		if(tvPhoneNumEnsureErr.getVisibility()==View.INVISIBLE){
			tvPhoneNumEnsureErr.setVisibility(View.VISIBLE);
		}
		tvPhoneNumEnsureErr.setText("");
		tvPhoneNumEnsureErr.setCompoundDrawablesWithIntrinsicBounds(R.drawable.right,0,0,0);
	}

	/**
	 * 再次输入提示错误
	 */
	private void setPhoneNumInputEnsureError(){
		if(tvPhoneNumEnsureErr.getVisibility()==View.INVISIBLE){
			tvPhoneNumEnsureErr.setVisibility(View.VISIBLE);
		}
		tvPhoneNumEnsureErr.setCompoundDrawablesWithIntrinsicBounds(R.drawable.error,0,0,0);
		tvPhoneNumEnsureErr.setText(R.string.action_input_phone_number_error2);
	}


	/**
	 * 话费券领取协议扩展参数
	 * @param mobileNum
	 * @param ticketCount
	 * @return
	 */
	private String buildExtXml(String mobileNum, int ticketCount) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("utf-8", null);
			serializer.startTag(null, "propext");
			serializer.attribute(null, "tel",mobileNum);
			serializer.attribute(null, "type", 2+"");
			serializer.attribute(null, "usecount", ticketCount+"");
			serializer.endTag(null, "propext");
			serializer.endDocument();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}


	/**
	 * 控件可以输入
	 */
	private void enabled() {
		tvNum.setTextColor(Color.argb(255, 255, 255, 255));
		tvNumEnsure.setTextColor(Color.argb(255, 255, 255, 255));
		mUrlText.setTextColor(Color.argb(255, 244, 216, 96));
		etPhoneNum.setBackgroundColor(Color.argb(255, 255, 255, 255));
		etPhoneNumEnsure.setBackgroundColor(Color.argb(255, 255, 255, 255));
		etPhoneNum.setEnabled(true);
		etPhoneNum.setFocusable(true);    //与disable对应，为解决htc问题
		etPhoneNum.setFocusableInTouchMode(true);  //htc,如果设置过focusable为false,则必须设置这个才可以编辑，否则不可获得焦点
		etPhoneNum.requestFocus();
		etPhoneNumEnsure.setEnabled(true);
		etPhoneNumEnsure.setFocusable(true);
		etPhoneNumEnsure.setFocusableInTouchMode(true);
		mUrlText.setEnabled(true);
		tvGetTicket.setClickable(true);
		tvGetTicket.setEnabled(true);
	}


	/**
	 * 控件不可以输入
	 */
	private void disabled() {
		tvNum.setTextColor(Color.argb(128, 255, 255, 255));
		tvNumEnsure.setTextColor(Color.argb(128, 255, 255, 255));
		etPhoneNum.setBackgroundColor(Color.argb(128, 255, 255, 255));
		etPhoneNumEnsure.setBackgroundColor(Color.argb(128, 255, 255, 255));
		etPhoneNum.setEnabled(false);
		etPhoneNum.setFocusable(false);
		etPhoneNum.setFocusableInTouchMode(false);
		etPhoneNumEnsure.setEnabled(false);
		etPhoneNumEnsure.setFocusable(false);     //htc手机必须设置这个才不可点击
		etPhoneNumEnsure.setFocusableInTouchMode(false);   //与enable一致,htc
		tvGetTicket.setClickable(false);
		tvGetTicket.setEnabled(false);
	}



}
