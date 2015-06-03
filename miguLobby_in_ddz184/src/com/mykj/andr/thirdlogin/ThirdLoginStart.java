package com.mykj.andr.thirdlogin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;
import com.MyGame.Midlet.util.HttpUtil;
import com.MyGame.Midlet.util.IGetRequest;
import com.MyGame.Midlet.util.Util;
import com.login.view.AccountManager;
import com.login.view.LoginViewCallBack;
import com.mingyou.accountInfo.AccountItem;
import com.mingyou.accountInfo.LoginInfoManager;
import com.mykj.game.utils.CenterUrlHelper;

public class ThirdLoginStart {

	private static final String TAG = ThirdLoginStart.class.getSimpleName();
	
	public static final String token_key="CMCC_TOKEN";
	
	// 第三方获取ip和port
	private static final String trdUrl = "http://qpwap.cmgame.com/get_third_entry.php";

	private Context mContext;
	
	private static ThirdLoginStart instance = null;
	
	private LoginViewCallBack mLoginCallBack;

	private static final String gid = "10022";

	private static final String sid = "0";

	// 方法名
	private static final String method = "gettoken";

	// 平台ID 0：名游 2：百度 3：移动
	private static final int pid = 3;

	// 第三方类型 1-乐逗 3-极游 6-V5
	private static final String type = "46";

	private static final int GETTOKENSUSCCEED = 0;// 获取token成功标记


	private ThirdLoginStart(Context context) {
		mContext = context;
	}

	/**
	 * 单例类
	 * 
	 * @param act
	 * @return
	 */
	public static ThirdLoginStart getInstance(Context context) {

		if (instance == null) {
			instance = new ThirdLoginStart(context);
		}
		return instance;
	}

	public void init(LoginViewCallBack callback) {
		mLoginCallBack = callback;
	}
	


	private void getTokenByThirdId(String url) {
		HttpUtil.httpGet(url, new IGetRequest() {

			@Override
			public void httpReqResult(String xmlStr) {

				int status = -1;
				String strStatus = getTagStr(xmlStr, "status");
				if (!strStatus.equals("")) {
					status = Integer.parseInt(strStatus);
				}

				if (status == GETTOKENSUSCCEED) {
					// 对于需要绑定的第三方账号绑定成功则写成功标志，下一次则不用再绑定
					String token = getTagStr(xmlStr, "token");

					Configs.setStringSharedPreferences(mContext, token_key, token);
					
					/*AccountItem item = new AccountItem(null, null, token,
							AccountItem.ACC_TYPE_THIRD, 0, null, null);
					
					LoginInfoManager.getInstance().updateAccInfo(item);
					LoginInfoManager.getInstance().setCurAccountItem(item);
					AccountManager.getInstance().quickEntrance(mLoginCallBack);*/
					AccountManager.getInstance().thirdQuickEntrance(mLoginCallBack,token);
				} else {
					String errMsg = getTagStr(xmlStr, "msg");
					Log.e(TAG, errMsg);

				}

			}

		});
	}

	public void getTokenUrl(final String key) {
		if (!Util.isEmptyStr(key)) {
			HttpUtil.httpGet(trdUrl, new IGetRequest() {

				@Override
				public void httpReqResult(String buf) {
					// TODO Auto-generated method stub
					List<String> urls = getTagMultiStr(buf, "url");
					if (urls.size() > 0) {
						int randInt = new Random().nextInt(urls.size());
						String url = urls.get(randInt);

						if (!com.mykj.game.utils.AppConfig.isOuterNet()) {
							url = com.mykj.game.utils.AppConfig
									.readPropertyValue("reqUrl");
						}

						url = url + "?" + getTokenUrlParam(key);
						getTokenByThirdId(url);
					}

				}

			});
		} else {

		}

	}

	private List<String> getTagMultiStr(String xmlStr, String tag) {
		String tagStr = "";
		String curStr = xmlStr;
		List<String> lstStr = new ArrayList<String>();
		while (curStr.indexOf(tag) > 0) {
			// 获取token成功
			int beginIndex = curStr.indexOf(tag + "=\"")
					+ (tag + "=\"").length();
			curStr = curStr.substring(beginIndex, curStr.length());
			tagStr = curStr.substring(0, curStr.indexOf("\""));
			lstStr.add(tagStr);
		}
		return lstStr;

	}

	/**
	 * 获取请求token的url
	 * 
	 * @param openId
	 * @param sessionId
	 * @return
	 */
	private String getTokenUrlParam(String uid) {
		String params = getParamsStr(uid);
		String sign = getSign(params);
		return params + sign;
	}

	/**
	 * 获取参数字符串
	 * 
	 * @param uid
	 * @return
	 */
	private String getParamsStr(String uid) {
		String cid = AppConfig.channelId; // AppConfig.channelId;
		String scid = AppConfig.childChannelId; // AppConfig.childChannelId;
		StringBuffer sb = new StringBuffer();
		sb.append("method=").append(method);
		sb.append('&').append("pid=").append(pid);
		sb.append('&').append("cid=").append(cid);
		sb.append('&').append("scid=").append(scid);
		sb.append('&').append("oid=").append(uid);
		sb.append('&').append("sid=").append(sid);
		sb.append('&').append("gid=").append(gid);
		sb.append('&').append("ext=").append("");
		sb.append('&').append("type=").append(type);
		// gameid 100斗地主 0大厅登录
		sb.append('&').append("gameid=").append("100");
		sb.append('&').append("format=").append("xml");

		return sb.toString();
	}

	/**
	 * 采用MD5算法的校验串
	 */
	private static String getSign(String params) {
		// 去除"&"
		String[] s_A = params.split("&");
		// 排序
		Arrays.sort(s_A);
		// 组合
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s_A.length; i++) {
			sb.append(s_A[i]);
		}
		// 加上密钥
		sb.append("secret=%s");
		// 生成参数串
		StringBuffer result_sb = new StringBuffer();
		result_sb.append("&sig=");
		result_sb.append(CenterUrlHelper.md5(sb.toString()));

		return result_sb.toString();
	}

	/**
	 * 通过标签解析相关返回内容
	 */
	private String getTagStr(String xmlStr, String tag) {
		String tagStr = "";
		if (xmlStr.indexOf(tag) > 0) {
			// 获取token成功
			int beginIndex = xmlStr.indexOf(tag + "=\"")
					+ (tag + "=\"").length();
			String tokenStr = xmlStr.substring(beginIndex, xmlStr.length());
			tagStr = tokenStr.substring(0, tokenStr.indexOf("\""));
		}
		return tagStr;

	}

}