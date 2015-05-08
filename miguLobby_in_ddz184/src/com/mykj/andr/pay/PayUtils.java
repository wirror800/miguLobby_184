package com.mykj.andr.pay;


import android.R.bool;
import android.content.Context;

import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;
import com.unicom.dcLoader.Utils;

public class PayUtils {

	private static final String ANDROID_ID="02";

	/**
	 * 购买数量限制
	 * @param statusBit
	 * @param isCmnetBuyLimited
	 * @return
	 */
	public static int getCmnetBuyLimitedCount(int statusBit,
			boolean isCmnetBuyLimited) {

		int limited = 0;

		limited = statusBit >> 8;

		if (limited == 0 && isCmnetBuyLimited) {
			limited = 5;
		}
		return limited;

	}


	/**
	 * 购买次数限制
	 * @param statusBit
	 * @return
	 */
	public static int getBuyLimitTime(int statusBit) {
		int limited;
		limited = statusBit >> 16;
		if (limited == 0) {
			limited = 60;
		}
		return limited;
	}




	/**
	 * 4.5.2.19.签名信息获取:附加数据
	 * 
	 * @param operatorId
	 *            :运营商ID 1:移动 2:电信 3:联通
	 * @param platId
	 *            :平台ID 0：名游 2：百度 3：移动
	 * @param paytype
	 *            :是否上传支付版本号和支付列表
	 * @return
	 */
	public static String getPayExternal(Context context,int operatorId,boolean isPayAgain,int paytype,String retry) {
		// 系统id:此处02为android平台
		String osId = ANDROID_ID;  
		String plist = null;
		String macaddress=Util.getLocalMacAddress(context);
		macaddress=Util.getStringmacAddress(macaddress);   //正则处理
		String ipaddress =Util.getLocalIpAddress();

		
		
		int signType=PayManager.getSigntypeString(); //获取客户端默认支付类型  

		String imei=Util.getIMEI(context);
		String versionName=Util.getVersionName(context);

		int playid = FiexedViewHelper.getInstance().getGameType();
		int userId=FiexedViewHelper.getInstance().getUserId();
		// 组成信息
		StringBuffer sb = new StringBuffer();
		sb.append("<p>");
		sb.append("<op>").append(operatorId).append("</op>");
		sb.append("<pid>").append(AppConfig.plat_id).append("</pid>");
		sb.append("<os>").append(osId).append("</os>");
		sb.append("<cid>").append(AppConfig.channelId).append("</cid>");
		sb.append("<scid>").append(AppConfig.childChannelId).append("</scid>");
		sb.append("<gid>").append(AppConfig.gameId).append("</gid>");
		sb.append("<playid>").append(playid).append("</playid>");   //玩法选择
		
		if(paytype == PayManager.PAY_LIST_ALL){
			plist=PayManager.getPlistString(isPayAgain);//获取支付类型列表
			sb.append("<pver>").append(AppConfig.pay_version).append("</pver>");
			sb.append("<plist>").append(plist).append("</plist>");
			sb.append("<signtype>").append(signType).append("</signtype>");
		}else if(paytype == PayManager.PAY_LIST_PART){
			plist=PayManager.getSigntype();//获取支付类型列表
			sb.append("<pver>").append(AppConfig.pay_version).append("</pver>");
			sb.append("<plist>").append(plist).append("</plist>");
			sb.append("<signtype>").append(signType).append("</signtype>");
		}
		
		if(!Util.isEmptyStr(retry)){
			sb.append("<_retry>").append(retry).append("</_retry>");
		}
		
		String tags = context.getPackageName();
		android.util.Log.d(tags, "plist="+plist);

		/**联通校验专用**/
		sb.append("<macaddress>").append(macaddress).append("</macaddress>");//MAC玛
		sb.append("<ipaddress>").append(ipaddress).append("</ipaddress>");//网络IP
		sb.append("<gameaccount>").append(userId).append("</gameaccount>");//用户ID
		sb.append("<imei>").append(imei).append("</imei>");	//	imei
		sb.append("<versionName>").append(versionName).append("</versionName>");//版本号

		sb.append("</p>");

		return sb.toString();
	}

}
