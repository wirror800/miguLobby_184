package com.mykj.game.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.WindowManager;

import com.MyGame.Migu.R;

public class Util {
	private static final String TAG = "Util";

	/**
	 * md5验证
	 * 
	 * @param file
	 *            文件
	 * @param expectedMD5
	 *            md5验证码
	 * @return
	 */

	public static synchronized boolean downloadFileMD5Check(File f,
			String expectedMD5) {
		boolean flag = false;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(f);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = fis.read(b)) != -1) {
				md.update(b, 0, len);
			}

			if (md5(md).equals(expectedMD5)) {
				flag = true;
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return flag;
	}

	/**
	 * 获得md5验证码
	 * 
	 * @param md5
	 * @return
	 */
	public static synchronized String md5(MessageDigest md5) {
		StringBuffer strBuf = new StringBuffer();
		byte[] result16 = md5.digest();
		char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
				'b', 'c', 'd', 'e', 'f' };
		for (int i = 0; i < result16.length; i++) {
			char[] c = new char[2];
			c[0] = digit[result16[i] >>> 4 & 0x0f];
			c[1] = digit[result16[i] & 0x0f];
			strBuf.append(c);
		}

		return strBuf.toString();
	}

	public static String md5(String string) {
		if (isEmptyStr(string)) {
			return "";
		}
		try {
			return getMD5(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}



	private static String getMD5(byte[] source) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			StringBuffer result = new StringBuffer();
			for (byte b : md5.digest(source)) {
				result.append(Integer.toHexString((b & 0xf0) >>> 4));
				result.append(Integer.toHexString(b & 0x0f));
			}
			return result.toString();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}



	/**
	 * bytes to kb
	 * @param bytes
	 * @return
	 */
	public   static  String bytes2kb( long  bytes)  {
		BigDecimal filesize  =   new  BigDecimal(bytes);
		BigDecimal kilobyte  =   new  BigDecimal( 1024 );
		float returnValue  =  filesize.divide(kilobyte,  2 , BigDecimal.ROUND_UP).floatValue();
		return (returnValue  +   "  KB " );
	}



	/**
	 * bytes to mb
	 * @param bytes
	 * @return
	 */
	public   static  String bytes2mb( long  bytes)  {
		BigDecimal filesize  =   new  BigDecimal(bytes);
		BigDecimal megabyte  =   new  BigDecimal( 1024 * 1024 );
		float  returnValue  =  filesize.divide(megabyte,  2 , BigDecimal.ROUND_UP).floatValue();
		return (returnValue  +   "  MB " );
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
	 * 删除存在的文件
	 * 
	 * @param fileName
	 */
	public static void deleteFile(File file) {
		try {
			if (file.exists())
				file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除目录下所有文件
	 * 
	 * @param path
	 */
	public static void deleteDir(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File subFile : files) {
					if (subFile.isDirectory())
						deleteDir(subFile);
					else
						subFile.delete();
				}
			}
			file.delete();
		}
	}

	/**
	 * 返回6位随机数字符串
	 * 
	 * @return
	 */
	public static String randString() {
		String ret = "";
		for (int i = 0; i < 6; i++) {
			int a = (int) (Math.random() * 10);
			ret += a;
		}
		return ret;
	}

	/**
	 * 私有协议版本约定方式
	 * 版本三位小数点， 填入int型 1-10,11-20,21-30填入 
	 * @param ver: 1.5.0，  
	 * @return 版本号
	 */
	public static int getProtocolCode(String ver){
		int code=0;
		String[] strs=ver.split("\\."); 
		if(strs!=null && strs.length==3){
			try{
				int front=Integer.parseInt(strs[0]);
				int middle=Integer.parseInt(strs[1]);
				int back=Integer.parseInt(strs[2]);

				code|=front<<20;
				code|=middle<<10;
				code|=back;
			}catch(NumberFormatException e){

			}

		}

		return code;
	}


	/**
	 * 发送短信权限
	 */
	public static void sendSMS(String phoneNum, String msg) {
		Log.e(TAG, "正在发送短信。");
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNum, null, msg, null, null);
	}

	/*
	 * 发送可判断发送状态的短信
	 */
	public static void sendTextSMS(String phoneNum, String msg, Context mContext) {
		/* 创建SmsManager对象 */
		SmsManager smsManager = SmsManager.getDefault();

		try {
			/* 创建自定义Action常数的Intent(给PendingIntent参数之用), 与商城接受一致 */
			Intent itSend = new Intent("SMS_SEND_ACTIOIN");

			/* sentIntent参数为传送后接受的广播信息PendingIntent */
			PendingIntent mSendPI = PendingIntent.getBroadcast(
					mContext.getApplicationContext(), 0, itSend, 0);

			/* 发送SMS短信，注意倒数的两个PendingIntent参数 */
			smsManager.sendTextMessage(phoneNum, null, msg, mSendPI, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	 * 用来判断应用是否运行.
	 * 
	 * @param context
	 * @param className
	 *            判断的服务名字：包名+类名
	 * @return true 在运行, false 不在运行
	 */

	public static boolean isAppRunning(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		int maxNum = 40;
		List<ActivityManager.RunningTaskInfo> runningTasks = am
				.getRunningTasks(maxNum);
		for (ActivityManager.RunningTaskInfo taskInfo : runningTasks) {
			String activitytop = taskInfo.topActivity.getClassName();
			Log.v(TAG, "activitytop=" + activitytop);
			if (activitytop.startsWith("com.mykj.game")) {
				Log.e(TAG, "com.mykj.game is running");
				return true;
			}

		}

		Log.v(TAG, "com.mykj.game is stop");
		return false;

	}

	/**
	 * 判断是否为ophone
	 * 
	 * @return
	 */
	private static byte isOMS = 0;

	public static boolean isOMS() {
		if (isOMS == 0) {
			isOMS = -1;
			try {
				File f_0 = new File("/opl/etc/properties.xml");
				File f_1 = new File("/opl/etc/product_properties.xml");
				if (f_0.exists() || f_1.exists()) {
					isOMS = 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (isOMS > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为cmwap
	 * 
	 * @param context
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static boolean isCMWap(Context context) {
		boolean res = false;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return res;
		}
		int nType = networkInfo.getType();
		String eInfo = networkInfo.getExtraInfo();
		if (nType == ConnectivityManager.TYPE_MOBILE && eInfo != null) {
			if (eInfo.toLowerCase().equals("cmwap")) {
				res = true;
			}
		}

		return res;
	}

	/**
	 * 获取手机IMSI
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMSI(Context context) {
		String imsi = "";
		try {
			TelephonyManager phoneManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = phoneManager.getSubscriberId();
			Log.v(TAG, imsi);
		} catch (Exception e) {
			Log.e(TAG, "getIMSI error!");
			imsi = "";
		}

		if (imsi == null) {
			imsi = "";
		}
		return imsi;
	}

	/**
	 * 获取手机IMEI
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {
		String imei = "";
		try {
			TelephonyManager phoneManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imei = phoneManager.getDeviceId();
		} catch (Exception e) {
			Log.e(TAG, "getIMEI error!");
			imei = "";
		}
		if (imei == null) {
			imei = "";
		}
		return imei;
	}

	
	/**
	 * 获取iccid 
	 * SIM卡序列号
	 * @param context
	 * @return
	 */
	public static String getICCID(Context context) {
		String iccid = "";
		try {
			TelephonyManager phoneManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			iccid = phoneManager.getSimSerialNumber();
		} catch (Exception e) {
			Log.e(TAG, "getIMEI error!");
			iccid = "";
		}
		if (iccid == null) {
			iccid = "";
		}
		return iccid;
	}

	


	/**
	 * 获得版本号，从配置文件中
	 * @param context
	 * @return
	 */
	private static String getVerNameFromAssert(Context context){
		
		String versionName="";
		try {
			Properties pro = new Properties();
			InputStream is = context.getAssets().open("channel.properties");
			pro.load(is);
			String tmpVersionName = pro.getProperty("versionName");

			versionName = new String(tmpVersionName.getBytes("ISO-8859-1"),"UTF-8");

			is.close();
			is = null;
		} catch (Exception e) {
			versionName="";
			Log.e(TAG, "AppConfig.loadVersion have Exception e = "+e.getMessage());
		}
		return versionName;

	}


	/**
	 * 获取已安装程序的版本
	 */
	private static String getAppVer(Context context, String packageName) {
		String ver = null;

		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
			ver = pi.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ver;
	}

	/**
	 * 获取当前版本号
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getVersionName(Context ctx) {
		String packageName=ctx.getPackageName();
		if(packageName.equals("com.MyGame.Midlet")){
			return getVerNameFromAssert(ctx);
		}else{
			return getAppVer(ctx, packageName);
		}

	}



	public static String getOPID(Context ctx) {
		// IMSI号前面3位460是国家，紧接着后面2位,00 02是中国移动，01是中国联通，03是中国电信。  
		String PID = "";
		String IMSI = getIMSI(ctx);
		try {
			if (IMSI != null) {
				PID = IMSI.substring(0, 5);
			}
		} catch (Exception e) {
			// sim 不存在情况
			PID = "";
		}
		return PID;
	}
	
	/**
	 * 判断是否移动网络
	 * @param context
	 * @return
	 */
	public static boolean providersNameIsYidong(Context context){
		String IMSI =  getIMSI(context);
		if(IMSI.startsWith("46000") || IMSI.startsWith("46002")){
			return true;
		}
		return false;
	}

	public static String getDevice() {
		return android.os.Build.DEVICE;
	}

	public static String getOSVerion() {
		return android.os.Build.VERSION.RELEASE;
	}

	/***
	 * @Title: getMobileModel
	 * @Description: 获取手机型号
	 * @return
	 * @version: 2013-2-26 上午11:34:16
	 */
	public static String getMobileModel() {
		return android.os.Build.MODEL;
	}

	/***
	 * @Title: getMobileBRAND
	 * @Description: 获取手机品牌
	 * @return
	 * @version: 2013-2-26 上午11:37:21
	 */
	public static String getMobileBRAND() {
		return android.os.Build.BRAND;
	}

	/**
	 * 获取系统剩余内存
	 * 
	 * @return
	 */
	public static String getSystemMemory(Context ctx) {
		String memory = "";
		try {
			ActivityManager _ActivityManager = (ActivityManager) ctx
					.getSystemService(Context.ACTIVITY_SERVICE);

			ActivityManager.MemoryInfo minfo = new ActivityManager.MemoryInfo();
			_ActivityManager.getMemoryInfo(minfo);
			memory = String.valueOf(minfo.availMem / (1024 * 1024)) + "MB";
		} catch (Exception e) {
		}
		return memory;
	}

	/**
	 * 获取屏幕分辩率
	 * 
	 * @param ctx
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getDisplayMetrics(Context ctx) {
		String metrics = "";
		try {
			Display display = ((WindowManager) ctx
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();
			metrics += width;
			metrics += "x";
			metrics += height;
		} catch (Exception e) {
		}

		return metrics;
	}



	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}
		return null;

	}





	public static String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	} 


	public static String getStringmacAddress(String mac){     
		String res="";
		try{
			String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？]"; 
			Pattern   p   =   Pattern.compile(regEx);    
			Matcher   m   =   p.matcher(mac);    
			res=m.replaceAll("").trim();    
		}catch(Exception e){
			res="";
		}
		return res;
	}


	/**
	 * 获取手机基本信息，生产标准xml
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhoneInfo(Context context) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("utf-8", null);
			serializer.startTag(null, "MoblieProperty");
			serializer.attribute(null, "mn", getMobileModel());
			serializer.attribute(null, "ss", getDisplayMetrics(context));
			serializer.attribute(null, "ms", getSystemMemory(context));
			serializer.attribute(null, "sn", getDevice());
			serializer.attribute(null, "sv", getOSVerion());
			serializer.attribute(null, "is", getIMSI(context));
			serializer.attribute(null, "ie", getIMEI(context));
			serializer.attribute(null, "ver", getVersionName(context));
			serializer.endTag(null, "MoblieProperty");
			serializer.endDocument();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	/**
	 * HTTP请求服务器的公共方法
	 * 
	 * @param url
	 *            服务器的url（包括参数）
	 * @param reCount
	 *            失败时的自动重试次数
	 * @return
	 */
	public static String getConfigXmlByHttp(String url, int reCount) {
		String re = null;
		try {
			for (int i = 0; i < reCount; i++) {
				if (i != 0) {
					// 重复尝试时休眠下
					Thread.sleep(30);
				}
				re = getConfigXmlByHttp(url);
				if (re != null && re.length() > 0) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return re;
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
		Log.v(TAG, "http get url=" + url + ". return string=" + strConfig);
		return strConfig;

	}

	/**
	 * 网络是否连通
	 */
	public static boolean isNetworkConnected(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo nInfo = cm.getActiveNetworkInfo();
				if (nInfo != null) {
					if (nInfo.isConnected()) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 是否是wifi连接
	 */
	@SuppressLint("DefaultLocale")
	public static boolean isWifi(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo nInfo = cm.getActiveNetworkInfo();
				if (nInfo != null) {
					return nInfo.getTypeName().toUpperCase().equals("WIFI");
				}
			}
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * @param context
	 * @return 返回网络类型 1 mean wifi 2 mean CMWAP 3 mean CMNET
	 */
	@SuppressLint("DefaultLocale")
	public static int getAPNType(Context context) {
		int netType = -1;
		int WIFI = 1;
		int CMWAP = 2;
		int CMNET = 3;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String eInfo = networkInfo.getExtraInfo();
			if (eInfo != null) {
				if (eInfo.toLowerCase().equals("cmnet")) {
					netType = CMNET;
				} else {
					netType = CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = WIFI;
		}
		return netType;
	}

	/**
	 * @param context
	 * @return 返回网络类型 wifi , CMWAP  ,CMNET
	 */
	@SuppressLint("DefaultLocale")
	public static String getAPNTypeString(Context context) {
		String netType="";
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null) {
			int nType = networkInfo.getType();
			if (nType == ConnectivityManager.TYPE_MOBILE) {
				String eInfo = networkInfo.getExtraInfo();
				if (eInfo != null) {
					netType=eInfo.toLowerCase();

				}
			} else if (nType == ConnectivityManager.TYPE_WIFI) {
				netType = "wifi";
			}
		}

		return netType;
	}


	/**
	 * SD卡是否挂载
	 */
	public static boolean isMediaMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * 获取SD卡的路径
	 */
	public static String getSdcardPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 获取普通图片下载目录
	 * @return
	 */
	public static String getIconDir(){
		String path;
		if(isMediaMounted()){
			path=Util.getSdcardPath()+AppConfig.DOWNLOAD_FOLDER+AppConfig.ICON_PATH;
		}else{
			path=((Activity)AppConfig.mContext).getFilesDir()+AppConfig.ICON_PATH;
		}
		return path;
	}

	/**
	 * 获取抽奖机图片下载目录
	 * @return
	 */
	public static String getLotteryDir(){
		String path;
		if(isMediaMounted()){
			path=Util.getSdcardPath()+AppConfig.DOWNLOAD_FOLDER+AppConfig.LOTTERYBMP_PATH;
		}else{
			path=((Activity)AppConfig.mContext).getFilesDir()+AppConfig.LOTTERYBMP_PATH;
		}
		return path;
	}

	/**
	 * 判断是否汉字
	 * 
	 * @param a
	 *            char
	 * @return
	 */
	public static boolean isChinese(char a) {
		return String.valueOf(a).matches("[\u4E00-\u9FA5]");
	}

	/**
	 * 判断是否word。（数字（0-9）、字母（a-z/A—Z)、下划线）
	 * 
	 * @param a
	 *            char
	 * @return
	 */
	public static boolean isWord(char a) {
		return String.valueOf(a).matches("[0-9a-zA-Z_]");
	}

	/**
	 * 字符串是否包含除字母，下划线，数字，中文字符外的字符
	 * 
	 * @param String
	 * @return
	 */
	public static boolean isIllegalCh(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!isChinese(str.charAt(i)) && !isWord(str.charAt(i))) {
				return true;

			}
		}
		return false;
	}



	/**
	 * 从url解析出fileName
	 */
	public static String getFileNameFromUrl(String strUrl) {
		String fileName = null;

		try {
			if (strUrl != null) {
				String[] tmpStrArray = strUrl.split("/");
				fileName = tmpStrArray[tmpStrArray.length - 1];
				if (fileName == null || fileName.trim().length() == 0) {
					fileName = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileName;
	}


	@SuppressLint("DefaultLocale")
	public static String getApkStringVerByFileName(String fileName) {
		String apkVer = null;

		try {
			String s = fileName.toLowerCase();
			int i = s.indexOf("ver");
			i += 3;
			if (i + 5 < s.length()) {
				apkVer = s.substring(i, i + 5);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return apkVer;
	}

	/**
	 * 检测程序是否已经安装
	 * 
	 * @param packageName
	 *            游戏入口Activity所在的包名。不能以“.”结尾
	 * @param activityName
	 *            游戏入口Activity的类名。以“.”开头
	 */
	public static boolean isActivityInstalled(Context context,
			String packageName, String activityName) {
		if (packageName == null || packageName.trim().length() == 0) {
			return false;
		}
		if (activityName == null || activityName.trim().length() == 0) {
			return false;
		}
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES);
			String s = packageName + activityName;
			for (int i = 0; i < pi.activities.length; i++) {
				ActivityInfo ai = pi.activities[i];
				if (ai.name.equals(s)) {
					return true;
				}
			}
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * 获取已安装程序的图标
	 */
	public static Drawable getIconFromApp(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
			return pi.applicationInfo.loadIcon(pm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 字符串保存为文件
	 * 
	 * @param path
	 *            文件全路径
	 * @param destStr
	 *            存储为文件的字符串
	 */

	public static void saveToFile(String path, String destStr) {
		if (destStr == null || destStr.trim().length() == 0) {
			Log.v(TAG, "destStr==null");
			return;
		}

		File file = new File(path);
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {

			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(destStr.getBytes("UTF-8"));
			fos.flush();
			fos.close();
		} catch (IOException e) {
			Log.v(TAG, "save to file is error");
		}
	}

	/**
	 * 从文件读取字符串
	 * 
	 * @param path
	 *            文件全路径
	 * @return 读取字符串
	 */
	public static String readFromFile(String path) {
		String str = null;
		if (path == null || path.trim().length() == 0) {
			return null;
		}

		File file = new File(path);

		if (file.exists() && file.isFile()) {
			int filelength = (int) file.length();
			byte[] filecontent = new byte[filelength];
			try {
				FileInputStream in = new FileInputStream(file);
				in.read(filecontent);
				in.close();
				str = new String(filecontent, "UTF-8");

			} catch (Exception e) {
				Log.v(TAG, "read file is error");
				return null;
			}

		}
		return str;
	}

	/**
	 * 将bytes数组保存到文件中
	 * 
	 * @param file
	 * @param bytes
	 */
	public static void saveToFile(File file, byte[] bytes) {
		if (file == null || bytes == null || bytes.length == 0) {
			Log.v(TAG, "bytes==null");
			return;
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			Log.v(TAG, "save to file is error");
		}
	}

	public static byte[] readBytesFromFile(String path) {
		if (path == null || path.trim().length() == 0) {
			return null;
		}

		File file = new File(path);

		if (file.exists() && file.isFile()) {
			int filelength = (int) file.length();
			byte[] filecontent = new byte[filelength];
			try {
				FileInputStream in = new FileInputStream(file);
				in.read(filecontent);
				in.close();

			} catch (IOException e) {
				Log.v(TAG, "read file is error");
				return null;
			}
			return filecontent;
		}
		return null;
	}

	public static byte[] readBytesFromFile(File file) {
		if (file != null && file.exists() && file.isFile()) {
			int filelength = (int) file.length();
			byte[] filecontent = new byte[filelength];
			try {
				FileInputStream in = new FileInputStream(file);
				in.read(filecontent);
				in.close();

			} catch (Exception e) {
				Log.v(TAG, "read file is error");
				return null;
			}
			return filecontent;
		}
		return null;
	}

	/**
	 * 安装APK
	 */
	public static void installApk(Context context, String apkFilePath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkFilePath),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/**
	 * get bitmap from assert
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getImageFromAssetFile(Context context, String fileName) {
		Bitmap image = null;
		try {
			AssetManager am = context.getAssets();
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (Exception e) {

		}
		return image;
	}

	/**
	 * get Drawable from assert
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Drawable getDrawableFromAssets(Context context,
			String fileName) {
		try {
			return Drawable.createFromStream(
					context.getAssets().open(fileName), fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static Drawable getDrawableFromFile(File pngFile, int density) {
		Bitmap bmp = BitmapFactory.decodeFile(pngFile.getPath());
		bmp.setDensity(density);

		return new BitmapDrawable(bmp);
	}

	/**
	 * 自适应式从图片获取Drawable，类似于把图片放入drawable目录一样
	 * 
	 * @param context
	 * @param pngFile
	 * @param density
	 * @return
	 */

	public static Drawable getDrawableFromFile(Context context, File pngFile,
			int density) {
		Bitmap bmp = BitmapFactory.decodeFile(pngFile.getPath());
		if (bmp != null)
			bmp.setDensity(density);

		return new BitmapDrawable(context.getResources(), bmp);
	}

	/**
	 * 获取drawable
	 * 
	 * @param pngFile
	 * @return
	 */
	public static Drawable getDrawableFromFile(File pngFile) {

		return Drawable.createFromPath(pngFile.getPath());
	}

	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(String fileName) {
		Bitmap image = null;
		AssetManager am = AppConfig.mContext.getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is).copy(
					Bitmap.Config.ARGB_8888, true);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * 将bitmap保存到sdcard里面
	 * 
	 * @param bitName
	 *            ：需要保存的文件名
	 * @param mBitmap
	 *            ：图片文件
	 */
	public static void saveBitmap(Context context, String bitName,
			Bitmap mBitmap) {
		String filePath = Util.getSdcardPath() + AppConfig.COMMENDATION;
		String fileName = bitName + ".png";
		File file = new File(filePath, fileName);
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}

		FileOutputStream fOut = null;
		if (!file.exists()) {
			try {
				file.createNewFile();
				fOut = new FileOutputStream(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(context, AppConfig.mContext.getResources().getString(R.string.ddz_file_save_path)+":" + file.getPath(), Toast.LENGTH_LONG)
		.show();
		// 2012-11-29
		mBitmap.recycle();

	}

	/**
	 * 判断一个drawable是否是有效的
	 * 
	 * @param drawable
	 * @return
	 */
	public static boolean isDrawableAvailable(Drawable drawable) {
		return drawable != null && drawable.getIntrinsicHeight() > 0
				&& drawable.getIntrinsicWidth() > 0;
	}

	/**
	 * url 下载路径
	 * 注：当多线程下载的时候，第一次可能只有第一个文件下载成功，因为第一个线程创建的路径其他线程不能用，但是其他线程又不能再创建这个路径了
	 * 在此给出的建议是当多线程下载多个文件到同一个文件路径的情况下，有以下两种方式 1.在线程外创建路径
	 * 2.判断下载是否成功，若不成功则线程sleep一段时间再开启，这时第一个线程跑完，其创建的路径就可以被其他线程使用了 fullFileName
	 * 保留到本地的全路径，如mnt/sdcard/aaa/bbb.xml 返回文件路径
	 */
	public static boolean downloadResByHttp(String url, String fullFileName) {
		boolean res = false;
		if(isEmptyStr(url) || isEmptyStr(fullFileName)){
			return false;
		}
		try {
			int dirIndex = fullFileName.lastIndexOf("/");
			if (dirIndex > 0) {
				String dir = fullFileName.substring(0, dirIndex);
				File parent = new File(dir);
				if (!parent.exists()) {
					parent.mkdirs();
				}
			}
			File file = new File(fullFileName);
			if (file.exists()) {
				/**如果是图片，先判断图片是否正确，防止图片文件大小正确但是不可用*/
				if(fullFileName.endsWith(".png") || fullFileName.endsWith(".jpg")){
					if(isDrawableAvailable(getDrawableFromFile(AppConfig.mContext, file,
							DisplayMetrics.DENSITY_HIGH))){
						return true;
					}else{
						file.deleteOnExit();
					}
				}else{
					return true;
				}
			}

			File tmpFile = new File(fullFileName + ".tmp");
			if (!tmpFile.exists()) {
				tmpFile.createNewFile();
			}

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(httpParams, 30000);

			HttpGet httpGet = new HttpGet(url);

			long startPosition = tmpFile.length(); // 已下载的文件长度
			String start = "bytes=" + startPosition + "-";
			httpGet.addHeader("Range", start);

			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					|| httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
				HttpEntity entity = httpResponse.getEntity();

				InputStream inputStream = entity.getContent();
				long fileSize = entity.getContentLength();
				byte[] b = new byte[1024];
				int readedLength = -1;

				OutputStream outputStream = new FileOutputStream(tmpFile, true);
				while (((readedLength = inputStream.read(b)) != -1)) {
					outputStream.write(b, 0, readedLength);
				}
				inputStream.close();
				outputStream.close();
				tmpFile.renameTo(file);
				long localFilesize = file.length();
				Log.e("downloadFile", "fullname:" + fullFileName + "   "
						+ "filesize" + fileSize + "    " + "localSize"
						+ localFilesize);
				if (fileSize == localFilesize) {
					res = true;
				} else if (localFilesize > fileSize) {
					file.delete();
					res = false;
				} else {
					//downloadResByHttp(url, fullFileName);
					res = false;
				}
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "file download fail, " + e.toString());
			return false;
		} catch (IOException e) {
			Log.e(TAG, "file download fail, " + e.toString());
			return false;
		} catch (Exception e){  //极有可能出现其他错误，比如http://没写或者写2次，服务端下载地址可能配置错误
			Log.e(TAG, "file download fail, " + e.toString());
			return false;
		}
		return res;

	}

	/**
	 * 写入SharedPreferences数据
	 * 
	 * @param String
	 *            value
	 * */
	public static void setStringSharedPreferences(Context context, String key,
			String value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * 读取SharedPreferences数据
	 * 
	 * @return String value
	 * */
	public static String getStringSharedPreferences(Context context,
			String key, String defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPref.getString(key, defaultValue);
	}

	/**
	 * 写入SharedPreferences数据
	 * 
	 * @param boolean value
	 * */
	public static void setBooleanSharedPreferences(Context context, String key,
			boolean value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 读取SharedPreferences数据
	 * 
	 * @return boolean value
	 * */
	public static boolean getBooleanSharedPreferences(Context context,
			String key, boolean defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPref.getBoolean(key, defaultValue);
	}

	/**
	 * 写入SharedPreferences数据
	 * 
	 * @param int value
	 * */
	public static void setIntSharedPreferences(Context context, String key,
			int value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.putInt(key, value);
		editor.commit();
	}

	/**
	 * 读取SharedPreferences数据
	 * 
	 * @return int value
	 * */
	public static int getIntSharedPreferences(Context context, String key,
			int defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPref.getInt(key, defaultValue);
	}

	/**
	 * 写入SharedPreferences数据
	 * 
	 * @param long value
	 * */
	public static void setLongSharedPreferences(Context context, String key,
			long value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.putLong(key, value);
		editor.commit();
	}

	/**
	 * 读取SharedPreferences数据
	 * 
	 * @return long value
	 * */
	public static long getLongSharedPreferences(Context context, String key,
			long defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		return sharedPref.getLong(key, defaultValue);
	}

	/**
	 * 中文标点符号转换
	 * 中文转英文
	 * @param input
	 * @return
	 */
	public static String toDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	public static boolean isNumeric(String str){ 
		Pattern pattern = Pattern.compile("[0-9]*"); 
		return pattern.matcher(str).matches();    
	} 


	public static String removeBom(String in){
		if (in != null && in.startsWith("\ufeff")) {
			in = in.substring(1);
		}
		return in;
	}

}
