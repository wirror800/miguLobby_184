package com.mykj.andr.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class FeedbackInfoActivity extends Activity {

	
	private Context mContext;
	private TextView mTextLimit;

	private TextView tvServiceNum;
	 
	private Button mSend;

	private EditText mText;

	int max = 140;

	/** 用户ID */
	public static final String USER_ID = "user_id";
	/** 用户Token */
	public static final String USER_TOKEN = "user_token";
	/** 渠道ID */
	public static final String CHANNELID = "channelID";
	/** 子渠道ID */
	public static final String SUBCHANNELID = "sub_channelID";

	public static final String MOBILE_CODE_TAIL = "02ANDROID1";

	public int userID = 0;
	public String userToken = "";
	public String channelId = "";
	public String sub_channelId = "";
	public String ver = "";
	public String phoneInfo = "";
	public String sugmsg = "";

	public static final short LS_TRANSIT_LOGON = 18;

	public static final short MSUB_SUGGEST_INFO_COMMIT = 118;

	public static final short MSUB_SUGGGEST_RESLUT_RESP = 119;

	public static final int HANDLER_COMMIT_SUCCESS = 10;

	public static final int HANDLER_COMMIT_FAIL = 20;

	public static final int HANDLER_FAIL = 30;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_info);
		mContext=this;
		
		tvServiceNum=(TextView) findViewById(R.id.service);
		tvServiceNum.setText(getString(R.string.service_info)+ServerDialog.SERVER_PHONE);
		
		userID=FiexedViewHelper.getInstance().getUserId();

		userToken = FiexedViewHelper.getInstance().getUserToken();
		
		ver = Util.getVersionName(mContext);
		
		phoneInfo = Util.getPhoneInfo(mContext);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		init();
		receiveFeedback();
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
		final Resources resource = this.getResources();
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.suggestion);
		findViewById(R.id.tvBack).setOnClickListener(new OnClickListener() {    //返回
			@Override
			public void onClick(View v) {

				finish();
			}
		});
		mText = (EditText) findViewById(R.id.note);
		TextView mTextView = (TextView) findViewById(R.id.textTest);

		mTextView.setText(R.string.back_info);
		mTextLimit = (TextView) findViewById(R.id.textLimit);

		mTextLimit.setText(resource.getString(R.string.feedback_you_can_input) + max + resource.getString(R.string.feedback_input_word_1) + "140" + resource.getString(R.string.feedback_input_word_2));

		mText = (EditText) findViewById(R.id.note);
		mText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				int num = max - s.length();
				mTextLimit.setText("还可以输入" + num + "字 ( 共140字 )");
			}
		});

		//发送意见箱
		mSend = (Button) findViewById(R.id.feedback_send);
		mSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sugmsg = mText.getText().toString();
				if (!sugmsg.equals("")) {
					sendFeedBack(sugmsg);
				} else {
					Toast.makeText(mContext, resource.getString(R.string.feedback_input_content_empty),Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void sendFeedBack(String sugmsg) {
		// 创建发送的数据包

		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userID);
		tdos.writeUTFShort(buildMsgXml(sugmsg));
		tdos.writeUTFShort(phoneInfo);

		NetSocketPak feedbackInfo = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_SUGGEST_INFO_COMMIT, tdos);
		// 发送协议
		NetSocketManager.getInstance().sendData(feedbackInfo); 
	}
	
	/****
	 * @Title: receiveFeedback
	 * @Description: 接受发送数据
	 * @version: 2013-1-12 上午10:24:36
	 */
	private void receiveFeedback(){
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON,
				MSUB_SUGGGEST_RESLUT_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					byte result = tdis.readByte();
					String errMsg = tdis.readUTFByte();

					if (result == 0) {// 成功
						Message msg = handler
								.obtainMessage(HANDLER_COMMIT_SUCCESS);
						msg.obj = errMsg;
						handler.sendMessage(msg);

					} else if (result == 1) {// 失败

						Message msg = handler
								.obtainMessage(HANDLER_COMMIT_FAIL);
						msg.obj = errMsg;
						handler.sendMessage(msg);
					}

				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(handler.obtainMessage(HANDLER_FAIL));
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		nPListener.setOnlyRun(false);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener); 
	}
	
	
	
	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_COMMIT_SUCCESS: // 发送数据成功
				//Toast.makeText(getApplication(), msg.obj.toString(),
				//		Toast.LENGTH_SHORT).show();
				if(msg.obj!=null){
					UtilHelper.showCustomDialog(mContext, (String)msg.obj);
				}
				mText.setText("");
				break;

			case HANDLER_COMMIT_FAIL: // 发送数据失败

				Toast.makeText(mContext, msg.obj.toString(),Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_FAIL:
				Toast.makeText(getApplication(), FeedbackInfoActivity.this.getResources().getString(R.string.feedback_submit_failed), Toast.LENGTH_SHORT).show();
				break;

			}
		}
	};

	public String buildMsgXml(String sugmsg) {

		StringBuffer sb = new StringBuffer();

		sb.append("<suggesbox token=\"").append(userToken);
		sb.append("\" uid=\"").append(userID);
		sb.append("\" sugmsg=\"").append(sugmsg);
		sb.append("\" mobilecode=\"").append(channelId)
				.append(MOBILE_CODE_TAIL);
		sb.append("\" ver=\"").append(ver);
		sb.append("\" subchannel=\"").append(sub_channelId);
		sb.append("\"/>");
		return sb.toString();
	}

}