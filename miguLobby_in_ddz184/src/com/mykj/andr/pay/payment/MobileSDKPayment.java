package com.mykj.andr.pay.payment;

import android.content.Context;
import android.util.Log;
import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.GameInterface.IPayCallback;

import com.mykj.andr.pay.PayManager;
import com.MyGame.Migu.R;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;


public class MobileSDKPayment {
	private final static String TAG = "MobileSDKPayment";


	private static MobileSDKPayment instance = null;

	private Context mContext;


	private MobileSDKPayment(){
	}
	public static MobileSDKPayment getInstance(Context context) {
		if (instance == null) {
			instance = new MobileSDKPayment();
		}

		instance.mContext = context;  //此处为了实时更新Context,解决弹框界面不一致

		return instance;
	}


	/**
	 * 移动支付SDK，支付结果回调
	 */
	IPayCallback billingCallback = new IPayCallback() {
		@Override
		public void onResult(int resultCode, String billingIndex, Object arg) {
			switch (resultCode) {
	          case BillingResult.SUCCESS:
//	        	  UtilHelper.showCustomDialog(mContext,
//							"购买成功");
	            break;
	          case BillingResult.FAILED:
//	        	  UtilHelper.showCustomDialog(mContext,
//							"购买成失败");
	        	  PayManager.getInstance(mContext).startPlistdialog(-1);
	              break;
	            default:
	              break;
			}

		}
	};

	/**
	 * 初始化支付
	 */
	public void initPayment(){


	}

	/**
	 * 获取支付参数
	 */
	public void Analytical(int shopID,String signParam){
		if(!Util.isEmptyStr(signParam)){
			String billingIndex =UtilHelper.parseStatusXml(signParam,"billingIndex");
			// CP服务器产生的订单号
			String cpparam = UtilHelper
					.parseStatusXml(signParam, "orderno");
			CallPayment(shopID,billingIndex,cpparam);
		}else{
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),Toast.LENGTH_SHORT).show();
		}
	}

	/***
	 * 调用支付SDK
	 */
	private void CallPayment(int shopID, String billingIndex,String cpparam){
		Log.v(TAG, "MobilePayment CallPayment start...");
		mBillingIndex=billingIndex;
		mCpparam=cpparam;
		//短信购买成功回调
		PayManager.getInstance(mContext).netReceive(shopID);
		GameInterface.doBilling(mContext, true, true, billingIndex,
				cpparam, billingCallback);
		
	}


    private String mBillingIndex="";
    private String mCpparam="";
    
    /**
     * 移动SDK支付重试接口
     */
    public void CmccSdkRetryBilling(){
    	
//    	GameInterface.retryBilling(mContext, true, true, mBillingIndex,
//    			mCpparam, billingCallback);
    	GameInterface.doBilling(mContext, true, true, mBillingIndex,
    			mCpparam, billingCallback);
    	
    }

}
