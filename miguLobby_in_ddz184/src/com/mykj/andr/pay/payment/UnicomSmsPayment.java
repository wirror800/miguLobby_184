package com.mykj.andr.pay.payment;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.mykj.andr.pay.PayManager;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class UnicomSmsPayment {
	private final static String TAG = "UnicomSmsPayment";

	private static UnicomSmsPayment instance = null;

	private Context mContext;

	private UnicomSmsPayment(){
	}
	public static UnicomSmsPayment getInstance(Context context) {
		if (instance == null) {
			instance = new UnicomSmsPayment();
		}
		instance.mContext = context;  //此处为了实时更新Context,解决弹框界面不一致
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
	public void Analytical(int shopID,String signParam,int from) {
		if (!Util.isEmptyStr(signParam)) {
			// 发送至号码
			String telSmsPhoneNumber = UtilHelper.parseStatusXml(signParam, "phonenum");
			// 短信内容:其中包括订单号
			String telSmsContent = UtilHelper.parseStatusXml(signParam, "pass");

			String telSmsOrder = UtilHelper.parseStatusXml(signParam, "orderId");
			CallPayment(shopID,telSmsPhoneNumber,telSmsContent,telSmsOrder,from);
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),Toast.LENGTH_SHORT).show();
		}
	}

	/***
	 * 调用支付SDK
	 */
	private void CallPayment(int shopID,String telSmsPhoneNumber,String telSmsContent,String telSmsOrder,int from) {
		Log.v(TAG, "TelecomPayment CallPayment start...");
		//短信购买成功回调
		PayManager.getInstance(mContext).netReceive(shopID);
		// 发送短信购买
		Util.sendTextSMS(telSmsPhoneNumber, telSmsContent, mContext);
		long startTime = System.currentTimeMillis();
		PayManager.setBuyingRecord(shopID, startTime);
		
		String content = mContext.getString(R.string.buysuccess);
		SpannableStringBuilder msg = new SpannableStringBuilder(content);
		int index = content.indexOf("\n");
		if(index > 0){
			msg.setSpan(new ForegroundColorSpan(0xffffff00),0,index,Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置指定位置文字的颜色
		}
		// 提示支付
		if (from != PayManager.SMS_PAY) {
			UtilHelper.showCustomDialogWithServer(mContext, msg, null,
					mContext.getString(R.string.i_know));
		}
	}

}
