package com.mykj.andr.pay.payment;

import mm.sms.purchasesdk.PurchaseSkin;
import mm.sms.purchasesdk.SMSPurchase;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.mykj.andr.pay.PayManager;
import com.mykj.andr.pay.mm.IAPHandler;
import com.mykj.andr.pay.mm.IAPListener;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class MMPayment {
	private final String TAG = "MMPayment";

	private static MMPayment instance = null;

	public SMSPurchase purchase;

	private static IAPListener mListener;

	private static Context mContext;



	private MMPayment(Context context){
		mContext=context;
	}

	public static MMPayment getInstance(Context context) {
		if (instance == null) {
			instance = new MMPayment(context);
		}
		mContext=context;
		return instance;
	}

	/**
	 * 初始化支付
	 */
	public void initPayment() {
		String appid = "300002717189";
		String appkey = "3CD1B8FF229C9A28";
		IAPHandler iapHandler = new IAPHandler((Activity) mContext);
		mListener = new IAPListener((Activity) mContext, iapHandler);
		purchase = SMSPurchase.getInstance();
//		purchase.setAppInfo(appid, appkey, PurchaseSkin.SKIN_SYSTEM_ONE); // 设置计费应用ID和Key (必须)
		purchase.setAppInfo(appid, appkey, PurchaseSkin.SKIN_SYSTEM_TWO); // 设置计费应用ID和Key (必须)
//		purchase.setAppInfo(appid, appkey, PurchaseSkin.SKIN_SYSTEM_THREE); // 设置计费应用ID和Key (必须)
		purchase.smsInit(mContext, mListener);
	}

	/**
	 * 获取支付参数
	 */
	public void Analytical(int shopID,String signParam) {
		if (!Util.isEmptyStr(signParam)) {
			String productnum = UtilHelper.parseStatusXml(signParam, "chargePoint");
			String product = UtilHelper.parseStatusXml(signParam, "orderno");
			CallPayment(shopID,productnum,product);
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),Toast.LENGTH_SHORT).show();
		}
	}

	/***
	 * 调用支付SDK
	 */
	public void CallPayment(int shopID,String productnum,String product) {

		Log.v(TAG, "MMPayment CallPayment start...");
		IAPListener.setContext((Activity) mContext);
		//购买回调
		PayManager.getInstance(mContext).netReceive(shopID);

		purchase.smsOrder(mContext, productnum, mListener, product);
	}

}
