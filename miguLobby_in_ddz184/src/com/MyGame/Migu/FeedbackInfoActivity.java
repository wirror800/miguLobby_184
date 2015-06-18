package com.MyGame.Migu;

import java.net.URLEncoder;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;
import com.MyGame.Migu.R;
import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.community.Community;

public class FeedbackInfoActivity extends Activity implements OnClickListener {

	private final static String TAG = "FeedbackInfoActivity";

	// 返回
	private TextView tvBack;

	private TextView mTextLimit;

	// private Button mBack, mSend;
	private Button mSend;

	private EditText mText;

	int max = 140;

	public static final String MOBILE_CODE_TAIL = "02ANDROID1";

	public int userID = 0;
	public String userToken = "";
	public String channelId = "";
	public String sub_channelId = "";
	public String ver = "";
	public String phoneInfo = "";
	public String sugmsg = "";
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


		userID = Community.getSelftUserInfo().userId;
		
		userToken = LoginInfoManager.getInstance().getToken();

		channelId = AppConfig.channelId;
		sub_channelId = AppConfig.childChannelId;
		ver = Configs.getVersionName(FeedbackInfoActivity.this);
		phoneInfo = Configs.getPhoneInfo(FeedbackInfoActivity.this);
		
		init();

	}

	private void init() {
		tvBack = (TextView) findViewById(R.id.tvBack);
		mText = (EditText) findViewById(R.id.note);

		tvBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mTextLimit = (TextView) findViewById(R.id.textLimit);

		mTextLimit.setText("还可以输入" + max + "字 ");

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
				mTextLimit.setText("还可以输入" + num + "字");
			}
		});

		mSend = (Button) findViewById(R.id.feedback_send);
		mSend.setOnClickListener(this);
	}

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

	/**
	 * 获取参数字符串
	 * 
	 * @param openId
	 * @param sessionId
	 * @return
	 */
	protected String getParamsStr(String xml, String extxml, String method) {

		StringBuffer sb = new StringBuffer();
		sb.append("method=").append(method);
		sb.append('&').append("xml=").append(URLEncoder.encode(xml));
		sb.append('&').append("extxml=").append(URLEncoder.encode(extxml));
		// sb.append('&').append("format=").append("xml");
		
		String url=sb.toString();
		Log.v(TAG, "用户反馈url="+url);
		return url;
	}

	// 请求意见提交
	class FeedbackTask extends AsyncTask<String, Void, String> {
		// 从网上下载图片
		@Override
		protected String doInBackground(String... params) {
			String feedbackInfo = Configs.getConfigXmlByHttp(params[0]);
			return feedbackInfo;
		}

		@Override
		protected void onPostExecute(String result) {
			if (null != result && result.length() > 0) {
				String[] statusInfo = result.split("#");
				//String status = statusInfo[0];
				String info = statusInfo[1];
				Toast.makeText(getApplication(), info, Toast.LENGTH_SHORT).show();
			}

		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.feedback_send:
			String msg = mText.getText().toString();
			if (!msg.equals("")) {

				FeedbackTask task = new FeedbackTask();
				task.execute(AppConfig.FEEDBACK_URL
						+ getParamsStr(buildMsgXml(msg), phoneInfo,
								"suggest_send"));
				Log.e("test", AppConfig.FEEDBACK_URL
						+ getParamsStr(buildMsgXml(msg), phoneInfo,
								"suggest_send"));
				Toast.makeText(getApplication(), "意见提交中...",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplication(), "提交内容不能为空...",
						Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
	}
	
	
}