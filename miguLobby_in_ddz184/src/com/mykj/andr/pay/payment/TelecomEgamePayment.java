package com.mykj.andr.pay.payment;

import android.content.Context;
import android.util.Log;
import cn.egame.terminal.paysdk.EgamePay;
import cn.egame.terminal.paysdk.EgamePayListener;

import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.pay.PayManager;
import com.mykj.andr.provider.GoodsItemProvider;
import com.MyGame.Migu.R;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class TelecomEgamePayment {
	private final static String TAG = "TelecomEgamePayment";

	private static TelecomEgamePayment instance = null;

	private static Context mContext;

	private TelecomEgamePayment(Context context) {
	}

	public static TelecomEgamePayment getInstance(Context context) {
		if (instance == null) {
			instance = new TelecomEgamePayment(context);
		}
		mContext = context;
		return instance;
	}

	/**
	 * 初始化支付
	 */
	public void initPayment() {

	}

	/**
	 * 获取支付参数
	 */
	public void Analytical(int shopID, String signParam) {
		if (!Util.isEmptyStr(signParam)) {
			String orderNo = UtilHelper.parseStatusXml(signParam,
					"orderId");
			int price = GoodsItemProvider.getInstance().findGoodsItemById(shopID).pointValue;
//			int price = Integer.parseInt(UtilHelper.parseStatusXml(signParam, "price"));
			CallPayment(shopID, price, orderNo);
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),
					Toast.LENGTH_SHORT).show();
		}
	}

	/***
	 * 调用支付SDK
	 */
	public void CallPayment(int shopID, int fee,String serialNo){
		Log.v(TAG, "TelecomEgamePayment CallPayment start...");
		//短信购买成功回调
		PayManager.getInstance(mContext).netReceive(shopID);
		EgamePay.payBySms(mContext, fee/100, serialNo, feeResultListener);
		// 提示支付
		if(mContext==null){
			return;
		}
//		UtilHelper.showCustomDialog(mContext,
//				mContext.getString(R.string.buysuccess));
	}

	private EgamePayListener feeResultListener = new EgamePayListener() {

		@Override
		public void paySuccess(int arg0, String arg1) {
			Toast.makeText(mContext, "爱游戏支付成功", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void payFailed(int arg0, String arg1, int arg2) {
			 Toast.makeText(mContext, "计费请求发送失败:"+arg2,
			 Toast.LENGTH_LONG).show();
			 PayManager.getInstance(mContext).startPlistdialog(-1);

		}

		@Override
		public void payCancel(int arg0, String arg1) {
			 PayManager.getInstance(mContext).startPlistdialog(-1);
		}
	};

}
