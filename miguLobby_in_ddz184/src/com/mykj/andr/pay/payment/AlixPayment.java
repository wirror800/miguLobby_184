package com.mykj.andr.pay.payment;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alipay.android.app.sdk.AliPay;
import com.mykj.andr.pay.PayManager;
import com.MyGame.Migu.R;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;


public class AlixPayment {
	private final static String TAG = "AlixPayment";

	private static AlixPayment instance = null;
	private Context mContext;


	private static final int RQF_PAY = 1;

	private static final int RQF_LOGIN = 2;
	
	private static final int ALI_PAY = 3;

	private AlixPayment(){
	}

	public static AlixPayment getInstance(Context context) {
		if (instance == null) {
			instance = new AlixPayment();
		}
		instance.mContext = context;
		return instance;
	}




	/**
	 * 初始化支付
	 */
	public void initPayment(){
		//支付出事操作
	}

	/**
	 * 获取支付参数
	 */
	public void Analytical(int shopID,String signParam){
		if(!Util.isEmptyStr(signParam)){
			CallPayment(signParam);

			PayManager.getInstance(mContext).netReceive(shopID);
		}else{
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),Toast.LENGTH_SHORT).show();
		}
	}

	/***
	 * 调用支付SDK
	 */
	private void CallPayment(final String signParam){
		Log.e(TAG,"signParam="+ signParam);
		// 获取订单组装字符串
		new Thread() {
			public void run() {
				// 获取Alipay对象，构造参数为当前Activity和Handler实例对象
				AliPay alipay = new AliPay((Activity) mContext, mHandler);
				// 调用pay方法，将订单信息传入
				String result = alipay.pay(signParam);
				// 处理返回结果
				Message msg = new Message();
				msg.what = RQF_PAY;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Result result = new Result((String) msg.obj);

			switch (msg.what) {
			case RQF_PAY:
			case RQF_LOGIN: 
				if(result.result_code.equals("4000") || result.result_code.equals("6001")){
					PayManager.getInstance(mContext).startPlistdialog(-1);
				}
				Toast.makeText(mContext, result.getResult(), Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;
			}
		};
	};

}
