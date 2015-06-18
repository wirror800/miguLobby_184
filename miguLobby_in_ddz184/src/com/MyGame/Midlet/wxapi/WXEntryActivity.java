package com.MyGame.Midlet.wxapi;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mykj.andr.model.HallDataManager;
import com.mykj.game.GlobalFiexParamer;
import com.mykj.game.ddz.DDZ;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {


	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;
	public static final int THUMB_SIZE = 150;

	private Context context;

	private OnClickListener mWxListener;
	private OnClickListener mTimelineListener;
	private OnClickListener mMsgListener;

	protected String title;

	protected String description;

	protected int resId;

	private Resources mResource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		mResource = this.getResources();
		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false);
		api.registerApp(WXConstants.APP_ID);
		api.handleIntent(getIntent(), this);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.getString("wx_title") != null) {
			title = bundle.getString("wx_title");
			description = bundle.getString("wx_description");
			String spkey = AppConfig.spKey==null?"":AppConfig.spKey;
			if(description!=null){
				description = description.replace("#spkey#",spkey);
			}
			resId = bundle.getInt("wx_thumb", 1);
		} else {
			WXUtil.PopularizeInfo popularizeInfo = WXUtil.getPopularizeInfo(AppConfig.spKey);
			title = popularizeInfo.title;
			description = popularizeInfo.Disc;
			resId = 0;

		}

		mWxListener = new View.OnClickListener() {
			public void onClick(View v) {

				if (!Util.isEmptyStr(AppConfig.spKey) && api.isWXAppInstalled()) {
					Toast.makeText(WXEntryActivity.this, 
							"对不起分享功能暂时无法使用", Toast.LENGTH_SHORT).show();
					/*SendMessageToWX.Req req = initializeWX(title, description,
							resId);
					req.scene = SendMessageToWX.Req.WXSceneSession;
					api.sendReq(req);*/
				} else {
					if (!api.isWXAppInstalled()) {
						Toast.makeText(context,
								mResource.getString(R.string.weixin_not_installed),
								Toast.LENGTH_SHORT).show();
					} else if (Util.isEmptyStr(AppConfig.spKey)) {
						Toast.makeText(context,
								mResource.getString(R.string.weixin_not_obtain_tuiguangma),
								Toast.LENGTH_SHORT).show();
						GlobalFiexParamer
						.getSPKey(AppConfig.gameId, HallDataManager
								.getInstance().getUserMe().userID);
					}

				}

			}
		};
		initializeListener();
		WXUtil.showAlert(this, mWxListener, mTimelineListener, mMsgListener,
				new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// Log.e("test", "onResume");
	}

	private void initializeListener() {

		mTimelineListener = new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(context, mResource.getString(R.string.weixin_friend_group_enable),
						Toast.LENGTH_SHORT).show();
			}
		};

		mMsgListener = new View.OnClickListener() {
			public void onClick(View v) {
				String mUrl = WXUtil.getWXUrl();
				if(mUrl == null || mUrl.trim().length() == 0){
					Toast.makeText(context, mResource.getString(R.string.weixin_obtaining_shared_address),
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (!Util.isEmptyStr(AppConfig.spKey)) {
					Uri smsUri = Uri.parse("smsto:");
					Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
					intent.putExtra("sms_body", description + mUrl);
					startActivity(intent);
				} else {
					Toast.makeText(context, mResource.getString(R.string.weixin_not_obtain_tuiguangma),
							Toast.LENGTH_SHORT).show();
				}

			}
		};
	}

	private SendMessageToWX.Req initializeWX(String title, String description,
			int resId) {
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = WXUtil.getWXUrl();
		WXMediaMessage msg = new WXMediaMessage(webpage);
		//		Resources res = getResources();
		//		resId = R.drawable.mark_icon;
		//		WXUtil.extractThumbNail(res, R.drawable.mark_icon, THUMB_SIZE,
		//				THUMB_SIZE, true)
		msg.setThumbImage(WXUtil.getWXIcon());
		msg.title = title;
		msg.description = description;

		final SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;

		return req;
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
		api.handleIntent(intent, this);
	}

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			goToGetMsg();
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp resp) {
		int result = 0;

		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success;
			finish();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}
		android.widget.Toast.makeText(this, result, Toast.LENGTH_LONG).show();

	}

	private void goToGetMsg() {
		// Intent intent = new Intent(this, GetFromWXActivity.class);
		// intent.putExtras(getIntent());
		// startActivity(intent);
		// finish();
		Toast.makeText(getApplication(), "testGotoGetMsg", Toast.LENGTH_SHORT)
		.show();
	}

	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
		WXMediaMessage wxMsg = showReq.message;
		WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

		StringBuffer msg = new StringBuffer(); // 组织一个待显示的消息内容
		msg.append("description: ");
		msg.append(wxMsg.description);
		msg.append("\n");
		msg.append("extInfo: ");
		msg.append(obj.extInfo);
		msg.append("\n");
		msg.append("filePath: ");
		msg.append(obj.filePath);
		Log.e("wx_ext_info", msg.toString());
		startActivity(new Intent(this, DDZ.class));
		finish();
	}


}