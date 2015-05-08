package com.MyGame.Midlet.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager.LayoutParams;

import com.MyGame.Midlet.MyGameMidlet;
import com.MyGame.Midlet.util.AppConfig;
import com.mingyou.community.Community;
import com.mingyou.community.MUserInfo;
import com.mykj.comm.log.MLog;

public class GamePlayerUtil {

	// 游戏玩家鉴权接口名称
	private static final String gamePlayerValidate = "gamePlayerValidate";

	// 游戏玩家订购接口名称
	private static final String gamePlayerOrder = "gamePlayerOrder";

	// 游戏玩家体验版
	private static final String GAME_PLAYER_EXPERIENCE = "802000023919";

	// 游戏玩家正式版
	// private static final String GAME_PLAYER = "500230544000";

	// 调用移动接口是否成功
	public static final String RS_OK = "000000";

	// 产品已订购，视为成功
	public static final String RS_REPEAT = "HW201005";

	// 余额不足
	public static final String RS_NO_MONEY = "HW201006";

	// 是否是游戏玩家
	public static final String IS_GAME_PLAYER = "yes";

	// 调用移动接口失败
	public static final String RS_NET_ERROR = "MY0001";

	// 默认的白名单地址，会被从IP列表解析出的数据重置
	private static String WHITENAME_CMWAP_URL = "http://g.10086.cn/home/interface/AutoRegLogin.php?app=qbdx";

	private static final String tokenHost = "game.10086.cn";

	private static final String loginHost = "g.10086.cn";

	private static final String loginApi = "home/interface/AutoRegLogin.php";

	private static final String tokenApi = "home/UcInterface/InterfaceIndex.php";

	private static final String appNo = "qbdx";

	private static final String key = "alksdasLDKFJAElkasdLKDFlke";

	private Context ctx;

	private static final String TAG = "GamePlayerUtil";

	private static final String smsPhone = "10658428";

	private static final String MY_ERROR = "MY0001";
	
	private static final int CONNECTION_TIMEOUT = 10000;

	public GamePlayerUtil(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * 是否是游戏玩家
	 * 
	 * @return
	 */
	public ResultObject isGamePlayer(String isPlayer) {
		String param = obtainGamePlayerValidateParam();
		ResultObject resultObject = null;
		if (param == null) {
			resultObject = new ResultObject(null, MY_ERROR, "网络异常，请检查网络情况！",
					"网络异常，请检查网络情况！", null);
		}else{
			resultObject = interGamePlayerInterface(gamePlayerValidate, param,
					isPlayer);
		}
		return resultObject;
	}

	/**
	 * 订购游戏玩家
	 */
	public ResultObject getPlayerOrder() {
		String param = obtainGamePlayerOrderParam();
		ResultObject resultObject = null;
		if (param == null) {
			resultObject = new ResultObject(null, MY_ERROR, "网络异常，请检查网络情况！",
					null, null);
		}else{
			resultObject = interGamePlayerInterface(gamePlayerOrder, param, null);
		}
		return resultObject;
	}

	private ResultObject interGamePlayerInterface(String interfaceName,
			String param, String isPlayer) {
		ResultObject resultObject = new ResultObject();
		MUserInfo userInfo = Community.getSelftUserInfo();
		String paramIsPlayer = isPlayer == null || "".equals(isPlayer) ? ""
				: "&isPlayer=" + isPlayer;
		if (param != null) {
			// 创建HttpClient实例
			HttpClient client = new DefaultHttpClient();
			
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
			
			// 根据URL创建HttpPost实例
			HttpPost post = new HttpPost(AppConfig.gamePlayerBaseUrl
					+ "?method=" + interfaceName + "&uid=" + userInfo.userId
					+ paramIsPlayer);

			try {

				StringEntity entity = new StringEntity(param, HTTP.UTF_8);
				entity.setContentType("text/xml charset=utf-8");
				post.setEntity(entity);

				// 发送请求并获取反馈
				HttpResponse response = client.execute(post);

				// 判断请求是否成功处理
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// 解析返回的内容
					String result = EntityUtils.toString(response.getEntity());

					resultObject = new ResultObject(parseStatusXml(result,
							"isGamePlayer"), parseStatusXml(result,
							"resultCode"), parseStatusXml(result, "resultMsg"),
							parseStatusXml(result, "msg"), parseStatusXml(
									result, "transactionId"));
				}
			} catch (Exception e) {
				Log.e("GamePlayerUtil", e.getMessage());
				resultObject = new ResultObject(null, MY_ERROR,
						"网络异常，请检查网络情况！", "网络异常，请检查网络情况！", null);
			}
			Log.e("GamePlayerUtil", resultObject.resultCode + ": "
					+ resultObject.resultMsg);
		}
		return resultObject;
	}

	/**
	 * 移动接口响应数据类
	 * 
	 * @author Administrator
	 * 
	 */
	public class ResultObject {
		private String isGamePlayer = ""; // 是否是游戏玩家
		private String resultCode;
		private String resultMsg;
		private String msg;
		private String transactionId;

		public ResultObject() {
		}

		public ResultObject(String isGamePlayer, String resultCode,
				String resultMsg, String msg, String transactionId) {
			this.isGamePlayer = isGamePlayer;
			this.resultCode = resultCode;
			this.resultMsg = resultMsg;
			this.msg = msg;
			this.transactionId = transactionId;
		}

		public String getIsGamePlayer() {
			return isGamePlayer;
		}

		public void setIsGamePlayer(String isGamePlayer) {
			this.isGamePlayer = isGamePlayer;
		}

		public String getResultCode() {
			return resultCode;
		}

		public void setResultCode(String resultCode) {
			this.resultCode = resultCode;
		}

		public String getResultMsg() {
			return resultMsg;
		}

		public void setResultMsg(String resultMsg) {
			this.resultMsg = resultMsg;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}
	}

	/**
	 * 获取游戏玩家校验参数
	 * 
	 * @return 游戏玩家校验参数
	 */
	private String obtainGamePlayerValidateParam() {
		String userId = getUserId();
		String result = null;
		if (null != userId) {
			result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<request>"
					+ "<authUserid>" + AppConfig.gamePlayerUserId
					+ "</authUserid>" + "<authPwd>" + AppConfig.gamePlayerPwd
					+ "</authPwd>" + "<channelCode>"
					+ AppConfig.gamePlayerChannelCode + "</channelCode>"
					+ "<userID>" + userId + "</userID>"
					+ "<userIDType>1</userIDType>" + "<transactionId>"
					+ getDateString(new Date(), "yyyyMMddHHmmss")
					+ createRandom(1000, 9999) + "</transactionId>"
					+ "</request>";
		}
		return result;
	}

	/**
	 * 获取订购游戏玩家参数
	 * 
	 * @return 订购游戏玩家参数
	 */
	private String obtainGamePlayerOrderParam() {
		String userId = getUserId();
		String result = null;
		if (null != userId) {
			result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<request>"
					+ "<authUserid>" + AppConfig.gamePlayerUserId
					+ "</authUserid>" + "<authPwd>" + AppConfig.gamePlayerPwd
					+ "</authPwd>" + "<channelCode>"
					+ AppConfig.gamePlayerChannelCode + "</channelCode>"
					+ "<userID>" + userId + "</userID>"
					+ "<userIDType>1</userIDType>" + "<packageId>"
					+ GAME_PLAYER_EXPERIENCE + "</packageId>"
					+ "<transactionId>"
					+ getDateString(new Date(), "yyyyMMddHHmmss")
					+ createRandom(1000, 9999) + "</transactionId>"
					+ "</request>";
		}
		return result;
	}

	private String getDateString(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	private int createRandom(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	public String parseStatusXml(String strXml, String tagName) {
		// boolean isParseSuccess = false;
		String tagStr = "";
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(strXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (p.getName().equals(tagName)) {
						tagStr = p.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				if (tagStr != null && tagStr.length() > 0) {
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tagStr;
	}

	/**
	 * @Title: showFastPromptDialog
	 * @Description: 提示对话框，用于购买提示
	 * @param ctx
	 * @param listener
	 * @param Msg
	 *            提示语
	 * @version: 2012-7-25 上午09:42:54
	 */
	public void dialog(DialogInterface.OnClickListener confirmListener,
			DialogInterface.OnClickListener cancelListener, String Msg) {
		AlertDialog.Builder builder = new Builder(ctx);
		builder.setMessage(Msg);

		builder.setTitle("提示");

		builder.setPositiveButton("确认", confirmListener);

		builder.setNegativeButton("取消", cancelListener);

		builder.create().show();
	}

	public ProgressDialog progressDialog() {
		ProgressDialog progress = new ProgressDialog(ctx);
		progress.setTitle("提示");
		progress.setMessage("请稍后...");
		DisplayMetrics dm = new DisplayMetrics();
		((MyGameMidlet) ctx).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		int width = screenW / 2 + 100;
		progress.show();
		progress.getWindow().setLayout(width, LayoutParams.WRAP_CONTENT);
		// ProgressDialog pd = ProgressDialog.show(ctx, "提示", "请稍后...", true);
		return progress;

	}

	WhiteName useRemoteTokenLogin() {
		int tryCount = 0;
		String token = "";
		while (tryCount < 1) {
			token = getRemoteToken();
			if (token != null && token.length() > 0) {
				break;
			}
			tryCount++;
		}
		if (token == null || token.length() == 0) {
			return null;
		}
		return getApiKey(token);
	}

	/**
	 * http 从移动获取apikey
	 * 
	 * @param token
	 * @return
	 */
	private WhiteName getApiKey(String token) {
		String IMSI = getIMSI(ctx);
		String parm = "?app=" + appNo + "&token=" + token + "&imsi=" + IMSI;
		String strUrl = "http://" + loginHost + "/" + loginApi + parm;
		Log.v(TAG, "getApiKey url= " + strUrl);

		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();

			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader buffer = new BufferedReader(in);
			String strLine;
			String data = "";

			while (((strLine = buffer.readLine()) != null)) {
				data += strLine;
				Log.v(TAG, strLine);
			}

			WhiteName ret = new WhiteName();

			// parse data
			if (data.indexOf("<status>200</status>") < 0) {
				Log.v(TAG, "getApiKey error");
				return null;
			}

			String tok1 = "", tok2 = "";
			int index1 = 0, index2 = 0;
			tok1 = "<uid>";
			tok2 = "</uid>";
			index1 = data.indexOf(tok1) + tok1.length();
			index2 = data.indexOf(tok2);
			if (index1 <= 0 || index2 < 0 || index2 < index1) {
				Log.v(TAG, "getApiKey error");
				return null;
			}
			ret.uid = data.substring(index1, index2);
			Log.v(TAG, "WhiteName.uid=" + ret.uid);

			tok1 = "<api_key>";
			tok2 = "</api_key>";
			index1 = data.indexOf(tok1) + tok1.length();
			index2 = data.indexOf(tok2);
			if (index1 <= 0 || index2 < 0 || index2 < index1) {
				Log.v(TAG, "getApiKey error");
				return null;
			}

			ret.api_key = data.substring(index1, index2);
			Log.v(TAG, "WhiteName.api_key=" + ret.api_key);

			Log.i("==WLan==", "uid = " + ret.uid);
			Log.i("==WLan==", "apikey = " + ret.api_key);

			Log.v(TAG, "获取移动apikey成功");
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.v(TAG, "getApiKey error");
		return null;
	}

	/**
	 * 获取手机IMSI
	 * 
	 * @param context
	 * @return
	 */
	public String getIMSI(Context context) {
		String imsi = "";
		try {
			TelephonyManager phoneManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = phoneManager.getSubscriberId();
			Log.v(TAG, imsi);
		} catch (Exception e) {
			Log.e(TAG, "getIMSI error!");
			imsi = "null";
		}
		return imsi;
	}

	/**
	 * 发送短信权限
	 */
	public static void sendSMS(String phoneNum, String msg) {
		Log.e(TAG, "正在发送短信。");
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNum, null, msg, null, null);
	}

	/**
	 * http 从移动获取token
	 * 
	 * @return
	 */
	private String getRemoteToken() {
		String ret = "";
		String IMSI = getIMSI(ctx);
		String randomNum = String.valueOf(createRandom(100000, 999999));
		String strSMS = IMSI + "@" + randomNum;

		Log.v(TAG, "正在发送短信 strSMS=" + strSMS);
		sendSMS(smsPhone, strSMS);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		int tryCount = 0;
		try {
			do {
				Thread.sleep(2000);
				ret = getRemoteToken_(IMSI, randomNum);
				if (ret != null && ret.length() > 0) {
					Log.v(TAG, "请求成功");
					break;
				}
				tryCount++;
			} while (tryCount < 5);
		} catch (Exception e) {
			Log.e(TAG, "移动获取白名单失败!!!");
		}
		return ret;
	}

	/**
	 * 计算并返回一个32位的MD5码
	 */
	@SuppressWarnings("finally")
	public static String getMD5(String str) {
		StringBuffer strBuf = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] result16 = md.digest();
			char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
					'a', 'b', 'c', 'd', 'e', 'f' };
			for (int i = 0; i < result16.length; i++) {
				char[] c = new char[2];
				c[0] = digit[result16[i] >>> 4 & 0x0f];
				c[1] = digit[result16[i] & 0x0f];
				strBuf.append(c);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			return strBuf.toString();
		}
	}

	/**
	 * http 请求移动token
	 * 
	 * @param imsi
	 * @param randomNum
	 * @return
	 */
	String getRemoteToken_(String imsi, String randomNum) {
		String ret = "";
		try {
			StringBuffer begintoken = new StringBuffer();
			begintoken.append("app=").append(appNo);
			begintoken.append('&').append("method=").append("getToken");
			begintoken.append('&').append("imsi=").append(imsi);
			begintoken.append('&').append("randomcode=").append(randomNum);
			String beginPara = begintoken.toString();
			begintoken.append('&').append("key=").append(key);
			String verifystring = getMD5(begintoken.toString());

			StringBuffer sbtoken = new StringBuffer();
			sbtoken.append(beginPara);
			sbtoken.append('&').append("verifystring=").append(verifystring);
			// 附加时间参数，防止浏览器或路由器或移动网关缓存导致重复请求无效 FWQ
			sbtoken.append('&').append("time=")
					.append(System.currentTimeMillis());

			String tokenparm = '?' + sbtoken.toString();

			String strUrl = "http://" + tokenHost + "/" + tokenApi + tokenparm;

			Log.v(TAG, "tokenUrl = " + strUrl);

			HttpGet _httpRequest = new HttpGet(strUrl);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
			HttpConnectionParams.setSoTimeout(httpParams, 30000);
			HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

			HttpClient httpClient = new DefaultHttpClient(httpParams);

			HttpResponse httpResponse = httpClient.execute(_httpRequest);
			int result = httpResponse.getStatusLine().getStatusCode();

			String data = "";

			Log.e(TAG, "result =" + result);

			if (result == HttpStatus.SC_OK) {
				String charSet = EntityUtils.getContentCharSet(httpResponse
						.getEntity());
				if (charSet == null)
					charSet = "UTF-8";

				data = EntityUtils.toString(httpResponse.getEntity(), charSet);
				Log.v(TAG, "返回白名单数据：" + data);
				if (data.indexOf("<status>200</status>") < 0) {
					Log.e(TAG, "getRemoteToken fail");
					return ret;
				}
				String tok1 = "<token>";
				int index1 = data.indexOf(tok1) + tok1.length();
				int index2 = data.indexOf("</token>");

				if (index1 == -1 || index2 == -1)
					return "";
				ret = data.substring(index1, index2);

			} else {
				Log.e(TAG, "getRemoteToken fail");
				return ret;
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();

		} catch (SocketTimeoutException e) {
			e.printStackTrace();

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (SocketException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
		}

		return ret;
	}

	private String getUserId() {
		// 获取圈圈用户ID
		SharedPreferences userInfo = ctx.getSharedPreferences("quan_user_info",
				0);
		String muidKey = getIMSI(ctx) + "_uid";
		String muid = userInfo.getString(muidKey, null);
		if (muid == null) {
			WhiteName whiteName = null;
			if (isCMWap(ctx)) {
				Log.v(TAG, "get apikey from cmwap");
				String apikeyStr = getConfigXmlByHttp(WHITENAME_CMWAP_URL);
				whiteName = parseWhiteNameData(apikeyStr);
			} else {
				whiteName = useRemoteTokenLogin();
			}
			if (null != whiteName) {
				muid = whiteName.uid;
				// muid = "132";
				userInfo.edit().putString(muidKey, muid).commit();
			} else {
				muid = null;
				MLog.e("获取白名单失败！");
			}
		}

		return muid;

	}

	/**
	 * 判断字符串是否为空 true is null
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmptyStr(String str) {
		if (str == null || str.trim().length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断是否为cmwap
	 * 
	 * @param context
	 * @return
	 */
	private static boolean isCMWap(Context context) {
		boolean res = false;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return res;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			if (networkInfo.getExtraInfo().toLowerCase().equals("cmwap")) {
				res = true;
			}
		}

		return res;
	}

	/**
	 * http 请求服务器 的公共方法 返回XML字符串
	 * */
	public static String getConfigXmlByHttp(String url) {
		if (url == null || url.trim().length() == 0) {
			return null;
		}
		String strConfig = null;
		HttpGet httpGet = new HttpGet(url);

		BasicHttpParams httpParams = new BasicHttpParams();

		HttpClient httpClient = new DefaultHttpClient(httpParams);

		try {
			// 设置代理，额外的处理，忽略异常, 仅在CMWAP联网时部分手机有用 ---FWQ 20130502
			String host = Proxy.getDefaultHost();
			int port = Proxy.getDefaultPort();
			if (host != null && port > 0) {
				HttpHost httpHost = new HttpHost(host, port);
				httpParams
						.setParameter(ConnRouteParams.DEFAULT_PROXY, httpHost);
			}
		} catch (Exception e1) {
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String charSet = EntityUtils.getContentCharSet(httpResponse
						.getEntity());
				if (charSet == null) {
					charSet = "UTF-8";
				}
				strConfig = EntityUtils.toString(httpResponse.getEntity(),
						charSet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return strConfig;

	}

	class WhiteName {
		public String uid;
		public String api_key;
	}

	private WhiteName parseWhiteNameData(String httpData) {
		WhiteName ret = new WhiteName();
		if (httpData == null) {
			return null;
		}
		if (httpData.indexOf("<status>200</status>") < 0) {
			return null;
		}
		int uidStart = httpData.indexOf("<uid>");
		int uidEnd = httpData.indexOf("</uid>");
		int apiKeyStart = httpData.indexOf("<api_key>");
		int apiKeyEnd = httpData.indexOf("</api_key>");
		String userID = null;
		String token = null;
		if (uidStart > 0 && uidEnd > 0) {
			userID = httpData.substring(uidStart + 5, uidEnd);
		}
		if (apiKeyStart > 0 && apiKeyEnd > 0) {
			token = httpData.substring(apiKeyStart + 9, apiKeyEnd);
		}
		if (userID != null && token != null) {
			ret.uid = userID;
			ret.api_key = token;

			return ret;
		}
		return null;
	}
}
