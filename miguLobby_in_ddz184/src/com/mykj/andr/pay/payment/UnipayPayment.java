package com.mykj.andr.pay.payment;

import android.content.Context;

import com.mykj.andr.pay.PayManager;
import com.MyGame.Migu.R;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;
import com.unicom.dcLoader.Utils;
import com.unicom.dcLoader.Utils.MonthType;
import com.unicom.dcLoader.Utils.UnipayPayResultListener;

/**
 * 联通Unipay1.3.1支付调用处理类
 * @author RenHaiyan
 *
 */
public class UnipayPayment {
	private final static String TAG = "UnipayPayment";

	private static UnipayPayment instance = null;

	private Context mContext;

//	private static final String APPID = "904516268520130319114019266400";// 应用编号
//	private static final String CPCODE="9045162685";//开发者编码
//	private static final String PRMCODE = "86005164";// cpid
//	private static final String COMPANY="深圳市同楼网络科技有限公司";// 公司名字
//	private static final String PHONE="0755-86219039";// 电话号码
//	private static final String CPID = "92007";// 开发商VAC资质编号

	private UnipayPayment() {
	}

	public static UnipayPayment getInstance(Context context) {
		if (instance == null) {
			instance = new UnipayPayment();
		}
		instance.mContext = context;  //此处为了实时更新Context,解决弹框界面不一致
		return instance;
	}

	/**
	 * 初始化支付
	 */
	public void initPayment() {
		//计费文件类型（0：通过打包系统发行， 1：获取计费文件自行打包，不通过联通打包系统发行）
		int type = 1;
		Utils.getInstances().initSDK(mContext, type, new PayResultListener());
	}

	/**
	 * 获取支付参数
	 */
	public void Analytical(int shopID, String signParam) {
		if (!Util.isEmptyStr(signParam)) {
			// 计费点编号截取的后三位
			String number = UtilHelper.parseStatusXml(signParam,"number");
			// CP服务器产生的订单号
			String orderid = UtilHelper.parseStatusXml(signParam, "orderid");

			CallPayment(shopID, number, orderid);
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 调用支付SDK
	 * @param shopID 商品ID
	 * @param number 计费点编号截取的后三位
	 * @param orderid 订单号
	 */
	private void CallPayment(int shopID, String number, String orderid) {
		// 接收服务器支付结果
		PayManager.getInstance(mContext).netReceive(shopID);
		
		// 订单号补0到24位
		int len = 24 - orderid.length();
		for (int i = 0; i < len; i++) {
			orderid += "0";
		}
		
		// 打印参数
		StringBuilder sb=new StringBuilder();
		sb.append("number=").append(number);
		sb.append(",oriderid=").append(orderid);
		String tag =sb.toString();
		Log.v(TAG, tag);
		
		if(number == null || "".equals(number)){
			Toast.makeText(mContext, "支付参数有误：number=" + number, Toast.LENGTH_LONG).show();
			return;
		}
		
		if(orderid == null || "".equals(orderid)){
			Toast.makeText(mContext, "支付参数有误：orderid=" + orderid, Toast.LENGTH_LONG).show();
			return;
		}
		
		/*
		 * 参数顺序：Context对象；计费点编号(的后三位)；计费点类型(SubMonth(按次代缴/真包月订购),UnSubMonth(按次代缴/真包月退订),Other(道具，关卡))；支付订单号；支付接口回调接口。
		number="007"; */
		Utils.getInstances().pay(mContext, number,MonthType.Other,orderid,new PayResultListener());
	}

	/**
	 * 联通的回调
	 * @author Administrator
	 */
	public class PayResultListener implements UnipayPayResultListener {
		
		public PayResultListener(){}

		@Override
		public void PayResult(String arg0, int flag, String arg2) {
			Toast.makeText(mContext, arg2, Toast.LENGTH_LONG).show();
			
			// flag为支付回调结果，flag2为回调类型，error为当前结果描述
			switch (flag) {
			case Utils.SUCCESS:
				//此处放置支付请求已提交的相关处理代码
				break;

			case Utils.FAILED:
				//此处放置支付请求失败的相关处理代码
				break;
				
			case Utils.CANCEL:
				//此处放置支付请求被取消的相关处理代码
				break;
				
			default:
				break;
			}

		}
		
		@Override
		public void FlowPackageResult(int arg0, String arg1, String arg2,
				String arg3, String arg4) {
			Toast.makeText(mContext, "arg0:"+arg0+";arg1:"+arg1+";arg2:"+arg2+";arg3:"+arg3+";arg4:"+arg4, Toast.LENGTH_LONG).show();
		}
	}
}
