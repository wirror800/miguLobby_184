package com.mykj.andr.pay.payment;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.mykj.andr.pay.PayManager;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.MobileHttpApiMgr;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;


public class MobileCMwapPayment{
	private final static String TAG = "MobilePayment";

	private static MobileCMwapPayment instance = null;
	/** 平台ID **/
	private static final int MOBILEPLATID = 3;

	private Context mContext;

	private MobileCMwapPayment(){
	}

	public static MobileCMwapPayment getInstance(Context context) {
		if (instance == null) {
			instance = new MobileCMwapPayment();
		}
		instance.mContext = context;  //此处为了实时更新Context,解决弹框界面不一致
		return instance;
	}
	/**
	 * 初始化支付
	 */
	public void initPayment(){


	}

	/**
	 * 获取支付参数
	 */
	public void Analytical(int shopID,String signParam,int from) {
		if (!Util.isEmptyStr(signParam)) {
			CallPayment(shopID,from);
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),Toast.LENGTH_SHORT).show();
		}
	}


	/***
	 * 调用支付SDK
	 */
	private void CallPayment(int shopID,int from) {
		Log.v(TAG, "AlixPayment CallPayment start...");


		PayManager.getInstance(mContext).netReceive(shopID);
		String mobileUserId = MobileHttpApiMgr.getInstance()
				.getOnlineGameUserId();
		
		int serviceId = MobileHttpApiMgr.getInstance()
				.getServiceId();
		
		if (!Util.isEmptyStr(mobileUserId)) {
			final int userId = FiexedViewHelper.getInstance().getUserId();
			final String token = FiexedViewHelper.getInstance().getUserToken();

			final short CURRENCY = 2;
			final short ONE = 1;
			final long CLISEC = 0;
			String channelId = AppConfig.channelId + "#"
					+ AppConfig.childChannelId + "#"+ serviceId;
			PayManager.getInstance(mContext).buyMarketGoods(userId, MOBILEPLATID, CURRENCY,
					shopID, token, channelId, mobileUserId, CLISEC, ONE,
					AppConfig.gameId);
			
			String content = mContext.getString(R.string.buysuccess);
			SpannableStringBuilder msg = new SpannableStringBuilder(content);
			int index = content.indexOf("\n");
			if(index > 0){
				msg.setSpan(new ForegroundColorSpan(0xffffff00),0,index,Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置指定位置文字的颜色
			}
			// 提示支付
			if(from != PayManager.SMS_PAY){
				UtilHelper.showTimeColorDialog(mContext,
						msg, null, false, true, mContext.getString(R.string.i_know));
			}
		}

	}






}
