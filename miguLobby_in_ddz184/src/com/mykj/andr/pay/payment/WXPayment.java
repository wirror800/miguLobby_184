package com.mykj.andr.pay.payment;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.mykj.andr.pay.PayManager;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayment {
	private final static String TAG = "WXPayment";

	private static WXPayment instance = null;

	private Context mContext;

	// APP_ID 替换为你的应用从官方网站申请到的合法appId
	public static final String APP_ID = "wxa87e621f0e7599f6";

	private IWXAPI api;

	private WXPayment() {
	}

	public static WXPayment getInstance(Context context) {
		if (instance == null) {
			instance = new WXPayment();
		}
		instance.mContext = context; // 此处为了实时更新Context,解决弹框界面不一致
		return instance;
	}

	/**
	 * 初始化支付
	 */
	public void initPayment() {
//		api = WXAPIFactory.createWXAPI(mContext, APP_ID);
//		api.registerApp(APP_ID);
	}

	/**
	 * 获取支付参数
	 */
	public void Analytical(int shopID, String signParam) {
		if (!Util.isEmptyStr(signParam)) {

			String appId = UtilHelper.parseStatusXml(signParam, "appId");

			String appKey = UtilHelper.parseStatusXml(signParam, "appKey");
			// 商户id
			String partnerId = UtilHelper
					.parseStatusXml(signParam, "partnerId");
			// 預支付訂單
			String prepayId = UtilHelper.parseStatusXml(signParam, "prepayId");
			// 隨機串
			String nonceStr = UtilHelper.parseStatusXml(signParam, "nonceStr");
			// 時間戳
			String timeStamp = UtilHelper
					.parseStatusXml(signParam, "timeStamp");
			// 商家根据文档填写的数据和签名
			String Package = UtilHelper.parseStatusXml(signParam,
					"package");
			try {
				Package = URLDecoder.decode(Package, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			PayManager.getInstance(mContext).netReceive(shopID);
			CallPayment(appId, appKey, partnerId, prepayId, nonceStr,
					timeStamp, Package);
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),
					Toast.LENGTH_SHORT).show();
		}
	}

	/***
	 * 调用支付SDK
	 */
	public void CallPayment(String appId, String appKey, String partnerId,
			String prepayId, String nonceStr, String timeStamp,
			String packageValue) {;
		api = WXAPIFactory.createWXAPI(mContext, appId);
		api.registerApp(appId);
		PayReq req = new PayReq();
		req.appId = appId;// 应用ID
		req.partnerId = partnerId;// 商户id
		req.prepayId = prepayId;// 預支付訂單
		req.nonceStr = nonceStr;// 隨機串
		req.timeStamp = timeStamp;// 時間戳
		req.packageValue = "Sign=" + packageValue;// 商家根据文档填写的数据和签名
//		req.packageValue = "Sign=WXPay";// 商家根据文档填写的数据和签名

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("appkey", appKey));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		req.sign = genSign(signParams);

		Log.e(TAG, signParams.toString());
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		if (!api.isWXAppInstalled()) {
			Toast.makeText(mContext,
					mContext.getString(R.string.weixin_not_installed_4pay),
					Toast.LENGTH_LONG).show();
		}else{
			api.sendReq(req);
		}
	}


	private String genSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());

		String sha1 = sha1(sb.toString());
		Log.d(TAG, "genSign, sha1 = " + sha1);
		return sha1;
	}

	public static String sha1(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes());

			byte[] md = mdTemp.digest();
			int j = md.length;
			char buf[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (Exception e) {
			return null;
		}
	}
}
