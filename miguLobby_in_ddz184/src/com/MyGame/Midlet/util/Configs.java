package com.MyGame.Midlet.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.mingyou.login.LoginHttp;
import com.mykj.comm.util.AndrUtil;

import debug.IP_CONFIG_FILE;



public class Configs extends IP_CONFIG_FILE{
	private static final String TAG="Configs";

	public static Context mContext=null;
	/**
	 * 从url解析出fileName
	 */
	public static synchronized String getFileNameFromUrl(String strUrl)
	{
		String fileName = null;

		if (strUrl != null)
		{
			String[] tmpStrArray = strUrl.split("/");
			fileName = tmpStrArray[tmpStrArray.length - 1];
			if (fileName.trim().length() == 0)
			{
				fileName = null;
			}
		}

		return fileName;
	}

	/**
	 * SD卡是否挂载
	 */
	public static boolean isMediaMounted()
	{
		boolean res;
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			res= true;
		}else if(getAvailableSize("/data/data")>20*1024*1024){
			res= true;
		}else{
			res= false;
		}
		return res;
	}

	
	
	 /**

     * 计算剩余空间

     * @param path

     * @return

     */ 

    public static long getAvailableSize(String path) 
    { 
        StatFs fileStats = new StatFs(path); 
        fileStats.restat(path); 
        return (long) fileStats.getAvailableBlocks() * fileStats.getBlockSize();  

    } 
	
	
	/**
	 * 获取SD卡的路径
	 */
	public static String getSdcardPath()
	{
		String path=null;
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
//			if(mContext!=null){
//				String path="data/data/"+mContext.getPackageName()+"/files";
//				File entry=new File(path);
//				if(entry.exists()){
//					Configs.deleteDir(entry);
//				}
//			}
			path= Environment.getExternalStorageDirectory().getPath();
		}else if((getAvailableSize("/data/data")>20*1024*1024)&&mContext!=null){		
			path= mContext.getFilesDir().getPath();			
		}
		
		return path;
	}


	
	
    /**
     * 判断是否汉字
     * @param a
     * @return
     */
    public static boolean isChinese(char a)
    {
    	return String.valueOf(a).matches("[\u4E00-\u9FA5]"); 
    }	
   
    /**
     * 判断是否word。（数字（0-9）、字母（a-z/A—Z)、下划线）
     * @param a		char
     * @return		
     */
    public static boolean isWord(char a)
    {
    	return String.valueOf(a).matches("[0-9a-zA-Z_]"); 
    }

	
    /**
     * 字符串是否包含除字母，下划线，数字，中文字符外的字符
     * @param str
     * @return
     */
    public static boolean isIllegalCh(String str){
    	for(int i=0;i<str.length();i++)
		{
			if(!isChinese(str.charAt(i)) && !isWord(str.charAt(i)))
			{
				return true;
	
			}
		}
    	return false;
    }
	
	
	
	/**
     * 用来判断应用是否运行.
     * @param context
     * @param className 判断的服务名字：包名+类名
     * @return true 在运行, false 不在运行
     */
      
    public static  String getGameTopActicity(Context context) {     
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);   
        int maxNum = 40; 
        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(maxNum);  
        for(ActivityManager.RunningTaskInfo taskInfo:runningTasks){  
        	String activitybase=taskInfo.baseActivity.getClassName();
            if(activitybase.startsWith("com.mykj")){
            	   String activitytop=taskInfo.topActivity.getClassName(); 
                   Log.v(TAG, "activitytop="+activitytop);
                   return activitytop;
            }
              
        }  
    	
        Log.v(TAG, "activitytop no find");
        return null;
   	
    }
	
	

    
	
	public static String getParmFormUrl(String strUrl){
		try{
			String[] tmpStrArray = strUrl.split("gameid=");
			int gameid=Integer.parseInt(tmpStrArray[1]);   
			return gameid+".xml";
		}catch(NullPointerException e){
			return null;
		}
	}




	/**
	 * 对下载的文件进行md5校验
	 */
	public static synchronized boolean downloadFileMD5Check(File f, String expectedMD5)
	{
		boolean flag = false;

		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(f);
			byte[] b = new byte[1024];
			int len = 0;
			while((len = fis.read(b)) != -1)
			{
				md.update(b, 0, len);
			}

			if(md5(md).equals(expectedMD5))
			{
				flag = true;
			}
			fis.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return flag;
	}

	/**
	 * 计算，返回一个32位的MD5码
	 */
	public static synchronized String md5(MessageDigest md5)
	{
		StringBuffer strBuf = new StringBuffer();
		byte[] result16 = md5.digest();
		char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (int i = 0; i < result16.length; i++)
		{
			char[] c = new char[2];
			c[0] = digit[result16[i] >>> 4 & 0x0f];
			c[1] = digit[result16[i] & 0x0f];
			strBuf.append(c);
		}

		return strBuf.toString();
	}






	/**
	 * 
	 * @param inContext
	 * @return wifi是否可以用
	 */
	public static boolean isWiFiActive(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}



	/**
	 * 加载gameLobbyProperties.properties文件
	 */
	public static  void loadGameLobbyProperties(Context context) {
		Properties p = new Properties();
		try {
			InputStream is = context.getAssets().open(AppConfig.PROPERTIES_FILE_NAME);
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			AppConfig.channelId = p.getProperty(AppConfig.PROPERTY_CHANNEL_ID,AppConfig.DEFAULT_CHANNEL_ID);			
			AppConfig.fid = p.getProperty(AppConfig.PROPERTY_FID, AppConfig.DEFAULT_FID);
			AppConfig.childChannelId = p.getProperty(AppConfig.PROPERTY_CHILD_CHANNEL_ID, AppConfig.DEFAULT_CHILD_CHANNEL_ID);

			try {
				int channel = Integer.parseInt(AppConfig.channelId);
				if(channel == 28){
					AppConfig.clientId = "8001";
				}else if(channel == 1){
					AppConfig.clientId = "8002";
				}else{
					AppConfig.clientId = (8000+channel)+"";
				}
			} catch (Exception e) {
				AppConfig.clientId = "8080";
			}
		}
	}


	/**
	 * 加载gamePlayerProperties.properties文件
	 * 主要配置游戏玩家参数
	 */
	public static  void loadGamePlayerProperties(Context context) {
		Properties p = new Properties();
		try {
			InputStream is = context.getAssets().open(AppConfig.GAME_PLAYER_PROPERTIES_FILE_NAME);
			p.load(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			AppConfig.gamePlayerUserId = p.getProperty(AppConfig.GAME_PLAYER_USERID);			
			AppConfig.gamePlayerPwd = p.getProperty(AppConfig.GAME_PLAYER_PWD);
			AppConfig.gamePlayerChannelCode = p.getProperty(AppConfig.GAME_PLAYER_CHANNELCODE);
			AppConfig.gamePlayerBaseUrl = p.getProperty(AppConfig.GAME_PLAYER_BASEURL);
			AppConfig.isGamePlayerEnable = Boolean.parseBoolean(p.getProperty(AppConfig.GAME_PLAYER_ENABLE));
		}
	}


	/**
	 * 加载gameLobbyProperties.properties文件
	 */
	public static String URL_CONFIG_FILENAME="url_config.xml";
	public static  void loadGameLobbyUrlConfig() {
		
		Properties p = new Properties();
		try {
//			InputStream is = new FileInputStream(new File(getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH,URL_CONFIG_FILENAME));
			InputStream is = new FileInputStream(new File(AndrUtil.getSDCardDir(),IP_CONFIG_FILE));
			p.load(is);
		} catch (Exception e) {
			Log.v(TAG, "loadGameLobbyUrlConfig no find");
		} finally {
			String isout=p.getProperty("isout");
			if(!Util.isEmptyStr(isout)){
				setIsOuterNet(Boolean.parseBoolean(isout));
			}
			String str1=p.getProperty("CONFIG_URL",null);	
			if(!isEmptyStr(str1)){
				AppConfig.CONFIG_URL =str1;
			}
			String str2=p.getProperty("GAMEINFO_URL", null);
			if(!isEmptyStr(str2)){
				AppConfig.GAMEINFO_URL =str2;
			}

			String str3=p.getProperty("MSG_URL", null);
			if(!isEmptyStr(str3)){
				AppConfig.MSG_URL =str3;
			}

			String str4=p.getProperty("PERSIONINFO_URL", null);
			if(!isEmptyStr(str4)){
				AppConfig.PERSIONINFO_URL =str4;
			}

			String str5=p.getProperty("FEEDBACK_URL", null);
			if(!isEmptyStr(str5)){
				AppConfig.FEEDBACK_URL =str5;
			}

			/**登录相关地址**/
			String str6=p.getProperty("loginHost", null);
			if(!isEmptyStr(str6)){
				LoginHttp.loginHost =str6;
			}

			/**帐号注册地址**/
			String str7=p.getProperty("resHost", null);
			if(!isEmptyStr(str7)){
				LoginHttp.resHost =str7;
			}
			
			/** 登录配置信息地址 **/
			String str8=p.getProperty("CONFIG_HOST", null);
			if(!isEmptyStr(str8)){
				LoginHttp.CONFIG_HOST =str8;
			}
			
			/**版本检测地址**/
			String str9=p.getProperty("MY_HOST", null);
			if(!isEmptyStr(str9)){
				LoginHttp.MY_HOST =str9;
			}
			
			/**获取登陆配置地址**/
			String str10=p.getProperty("httpLoginReq", null);
			if(!isEmptyStr(str10)){
				LoginHttp.httpLoginReq =str10;
			}
			
			String str11=p.getProperty("DelayTime", null);
			if(!isEmptyStr(str11)){
				long time=Long.parseLong(str11);
				AppConfig.WATCHDOG_DELAY =time;
			}
			
			String str12=p.getProperty("Cooldown_MIN", null);
			if(!isEmptyStr(str12)){
				AppConfig.Cooldown_MIN =Boolean.parseBoolean(str12);
			}
			
		}
	}

	/**
	 * 返回Drawable
	 */
	public static boolean downloadImgBitmap(String url, File file) {  
		boolean res=false;
		try {  
			File parent = file.getParentFile();
			if(!parent.exists()){
				parent.mkdirs();
			}

			if(!file.exists()){
				file.createNewFile();
			}

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(httpParams, 30000);

			HttpGet httpGet = new HttpGet(url);

			long startPosition=file.length();  //已下载的文件长度
			String start = "bytes=" + startPosition + "-";			
			httpGet.addHeader("Range",start);

			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					||httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT)
			{
				HttpEntity entity = httpResponse.getEntity();

				InputStream inputStream = entity.getContent();
				byte[] b = new byte[1024];
				int readedLength = -1;

				OutputStream outputStream = new FileOutputStream(file,true);
				while(((readedLength=inputStream.read(b)) != -1)){
					outputStream.write(b, 0, readedLength);
				}
				inputStream.close();
				outputStream.close();
				res=true;
			}
		} catch (ClientProtocolException e) {		

			Log.e(TAG, "file download fail");
		} catch (IOException e) {

			Log.e(TAG, "file download fail");
		}

		//return getDrawableFromFile(cxt,file,DisplayMetrics.DENSITY_HIGH);
		return res;

	}



	/**
	 * http 请求服务器
	 * 返回XML字符串,null return if fail
	 * */
	public static  String getConfigXmlByHttp(String url)
	{
		String strConfig = null;
		HttpGet httpGet = new HttpGet(url);

		BasicHttpParams httpParams = new BasicHttpParams();  

		HttpClient httpClient = new DefaultHttpClient(httpParams);

		try
		{
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				String charSet = EntityUtils.getContentCharSet(httpResponse.getEntity());
				if (charSet == null)
				{
					charSet = "UTF-8";
				}
				strConfig = EntityUtils.toString(httpResponse.getEntity(), charSet);
			}
		}
		catch (ClientProtocolException e)
		{
			return null;
		}
		catch (IOException e)
		{
			return null;
		}
		finally
		{
			httpClient.getConnectionManager().shutdown();
		}

		if (strConfig != null)
		{
			try
			{
				Pattern pattern = Pattern.compile("t=[0-9]+");
				Matcher matcher = pattern.matcher(strConfig);
				if(matcher.find())
				{
					Log.v(TAG, strConfig);
					String jumpUrl = url + "&" + matcher.group();
					Log.v(TAG, "jumpUrl = " + jumpUrl);
					return getConfigXmlByHttp(jumpUrl);
				}
			}
			catch (Exception e)
			{

			}
		}
		
		
		return strConfig;

	}




	/**
	 * 生成请求url
	 */
	public static String getConfigXmlUrl(String cmd)
	{
		StringBuffer sb = new StringBuffer();

		sb.append(AppConfig.CONFIG_URL);
		sb.append(cmd);
		sb.append("&");
		sb.append(AppConfig.PROPERTY_CHANNEL_ID);
		sb.append("=");
		sb.append(AppConfig.channelId);
		sb.append("&");
		sb.append(AppConfig.PROPERTY_FID);
		sb.append("=");
		sb.append(AppConfig.fid);

		return sb.toString();
	}

	/**
	 * 判断字符串是否为空  true is null
	 * @param str
	 * @return
	 */
	public static boolean isEmptyStr(String str){
		if(str==null||str.trim().length()==0){
			return true;
		}else{
			return false;
		}
	}


	/**
	 * 字符串保存为文件
	 * @param path 文件全路径
	 * @param destStr 存储为文件的字符串
	 */

	public static void saveToFile(String path,String destStr){
		if(destStr==null||destStr.trim().length()==0){
			Log.v(TAG, "destStr==null");
			return;
		}
		if(!isMediaMounted())
		{
			return;
		}
		File file=new File(path);
		File parent = file.getParentFile();
		if(!parent.exists()){
			parent.mkdirs();
		}
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException e){

			}
		}
		try{
			FileOutputStream fos=new FileOutputStream(file);
			fos.write(destStr.getBytes("UTF-8"));

			fos.close();
		}catch(IOException e){
			Log.v(TAG, "save to file is error");
		}
	}





	/**
	 * 从文件读取字符串
	 * @param path 文件全路径
	 * @return 读取字符串
	 */
	public static String readFromFile(String path){
		String str=null;
		if(!isMediaMounted()||path==null||path.trim().length()==0)
		{
			return null;
		}

		File file=new File(path);

		if(file.exists()&&file.isFile()){
			Long filelength = file.length();
			byte[] filecontent = new byte[filelength.intValue()];
			try {
				FileInputStream in = new FileInputStream(file);
				in.read(filecontent);
				in.close();
				str= new String(filecontent, "UTF-8");

			}catch(IOException e){
				Log.v(TAG, "read file is error");
				return null;
			}

		}
		return str;
	}

	/**
	 * 安装APK
	 */
	public static void installApk(Context context, String apkFilePath)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkFilePath), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	public static Bitmap getImageFromAssetFile(Context context,String fileName){  
		Bitmap image = null;  
		try{  
			AssetManager am = context.getAssets();  
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);  
			is.close();  
		}catch(Exception e){  

		}  
		return image;  
	}

	public static Drawable getDrawableFromAssets(Context context, String fileName)
	{
		try
		{
			return Drawable.createFromStream(context.getAssets().open(fileName), fileName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Drawable getDrawableFromFile(File pngFile, int density)
	{
		Bitmap bmp = BitmapFactory.decodeFile(pngFile.getPath());
		bmp.setDensity(density);

		return new BitmapDrawable(bmp); 
	}


	/**
	 * 自适应式从图片获取Drawable，类似于把图片放入drawable目录一样
	 * @param context
	 * @param pngFile
	 * @param density
	 * @return
	 */

	public static Drawable getDrawableFromFile(Context context,File pngFile, int density)
	{
		Bitmap bmp = BitmapFactory.decodeFile(pngFile.getPath());
		if(bmp != null)
			bmp.setDensity(density);

		return new BitmapDrawable(context.getResources(),bmp); 
	}


	public static Drawable getDrawableFromFile(File pngFile)
	{

		return Drawable.createFromPath(pngFile.getPath());
	}



	/**
	 * 判断是否有可用网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnect(Context context) { 
		try { 
			ConnectivityManager connectivity = (ConnectivityManager) context 
					.getSystemService(Context.CONNECTIVITY_SERVICE); 
			if (connectivity != null) { 

				// 获取网络连接管理的对象 
				NetworkInfo info = connectivity.getActiveNetworkInfo(); 

				if (info != null&& info.isConnected()) { 
					// 判断当前网络是否已经连接 
					if (info.getState() == NetworkInfo.State.CONNECTED) { 
						return true; 
					} 
				} 
			} 
		} catch (Exception e) { 
			Log.v(TAG,e.toString()); 
		} 
		return false; 
	}


	/**
	 * @param context
	 * @return 返回网络类型
	 *  1 mean wifi 
	 *  2 mean CMWAP 
	 *  3 mean CMNET 
	 */
	public static int getAPNType(Context context){ 
		int netType = -1;  
		int WIFI=1;
		int CMWAP=2;
		int CMNET=3;
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); 

		if(networkInfo==null){ 
			return netType; 
		} 
		int nType = networkInfo.getType(); 
		if(nType==ConnectivityManager.TYPE_MOBILE){ 

			Log.e("networkInfo.getExtraInfo()", "networkInfo.getExtraInfo() is "+networkInfo.getExtraInfo()); 

			if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet")){ 
				netType = CMNET; 
			} 
			else{ 
				netType = CMWAP; 
			} 
		} 
		else if(nType==ConnectivityManager.TYPE_WIFI){ 
			netType = WIFI; 
		} 
		return netType; 
	} 







	/**
	 * 删除目录下所有文件
	 * @param path
	 */
	public static void deleteDir(File file)
	{
		if (file.exists())
		{
			if (file.isDirectory())
			{
				File[] files = file.listFiles();
				for (File subFile : files)
				{
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
	 * Base64 encode and decode
	 * 
	 */
	private static final char[] legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

	/** Base64 encode the given data */
	public static String encode(byte[] data) {
		int start = 0;
		int len = data.length;
		StringBuffer buf = new StringBuffer(data.length * 3 / 2);

		int end = len - 3;
		int i = start;
		int n = 0;

		while (i <= end) {
			int d = ((((int) data[i]) & 0x0ff) << 16)
					| ((((int) data[i + 1]) & 0x0ff) << 8)
					| (((int) data[i + 2]) & 0x0ff);

			buf.append(legalChars[(d >> 18) & 63]);
			buf.append(legalChars[(d >> 12) & 63]);
			buf.append(legalChars[(d >> 6) & 63]);
			buf.append(legalChars[d & 63]);

			i += 3;

			if (n++ >= 14) {
				n = 0;
				buf.append("");
			}
		}

		if (i == start + len - 2) {
			int d = ((((int) data[i]) & 0x0ff) << 16)
					| ((((int) data[i + 1]) & 255) << 8);

			buf.append(legalChars[(d >> 18) & 63]);
			buf.append(legalChars[(d >> 12) & 63]);
			buf.append(legalChars[(d >> 6) & 63]);
			buf.append("=");
		} else if (i == start + len - 1) {
			int d = (((int) data[i]) & 0x0ff) << 16;

			buf.append(legalChars[(d >> 18) & 63]);
			buf.append(legalChars[(d >> 12) & 63]);
			buf.append("==");
		}

		return buf.toString();
	}

	private static int decode(char c) {
		if (c >= 'A' && c <= 'Z')
			return ((int) c) - 65;
		else if (c >= 'a' && c <= 'z')
			return ((int) c) - 97 + 26;
		else if (c >= '0' && c <= '9')
			return ((int) c) - 48 + 26 + 26;
		else
			switch (c) {
			case '+':
				return 62;
			case '/':
				return 63;
			case '=':
				return 0;
			default:
				throw new RuntimeException("unexpected code: " + c);
			}
	}

	/**
	 * Decodes the given Base64 encoded String to a new byte array. The byte
	 * array holding the decoded data is returned.
	 */

	public static byte[] decode(String s) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			decode(s, bos);
		} catch (IOException e) {
			throw new RuntimeException();
		}
		byte[] decodedBytes = bos.toByteArray();
		try {
			bos.close();
			bos = null;
		} catch (IOException ex) {
			System.err.println("Error while decoding BASE64: " + ex.toString());
		}
		return decodedBytes;
	}

	private static void decode(String s, OutputStream os) throws IOException {
		int i = 0;

		int len = s.length();

		while (true) {
			while (i < len && s.charAt(i) <= ' ')
				i++;

			if (i == len)
				break;

			int tri = (decode(s.charAt(i)) << 18)
					+ (decode(s.charAt(i + 1)) << 12)
					+ (decode(s.charAt(i + 2)) << 6)
					+ (decode(s.charAt(i + 3)));

			os.write((tri >> 16) & 255);
			if (s.charAt(i + 2) == '=')
				break;
			os.write((tri >> 8) & 255);
			if (s.charAt(i + 3) == '=')
				break;
			os.write(tri & 255);

			i += 4;
		}
	}

	public static String parseStatusXml(String strXml, String tagName) {
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
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// isParseSuccess = false;
		}
		// return isParseSuccess;
		return tagStr;
	}

	public static String getPhoneInfo(Context ctx) {
		String phoneInfo = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> <MoblieProperty ";
		try {
			phoneInfo += "mn=" + "\"" + android.os.Build.MODEL + "\" ";
			phoneInfo += "ss=" + "\"" + getDisplayMetrics(ctx) + "\" ";
			phoneInfo += "ms=" + "\"" + getSystemMemory(ctx) + "\" ";
			phoneInfo += "sn=" + "\"" + android.os.Build.DEVICE + "\" ";
			phoneInfo += "sv=" + "\"" + android.os.Build.VERSION.RELEASE + "\" ";
			phoneInfo += "is=" + "\"" + getImsiDirectly(ctx) + "\" ";
			phoneInfo += "ie=" + "\"" + getImeiDirectly(ctx) + "\" ";
			phoneInfo += "ver=" + "\"" + getVersionName(ctx) + "\" ";
			phoneInfo += "/>";
			Log.e("[PhoneInfo]", phoneInfo);
		} catch (Exception e) {
			//			e.printStackTrace();
			phoneInfo = "";
		}

		return phoneInfo;
	}

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

	
	public static String getSystemMemory(Context ctx) {
		String memory = "";
		try {
			ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
			ActivityManager.MemoryInfo minfo = new ActivityManager.MemoryInfo();
			am.getMemoryInfo(minfo);
			memory = String.valueOf(minfo.availMem / (1024 * 1024)) + "MB";
		} catch (Exception e) {
		}
		return memory;
	}

	
	
	public static String getImsiDirectly(Context ctx) {
		String imsi = "";
		try {
			TelephonyManager phoneManager = (TelephonyManager) ctx
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = phoneManager.getSubscriberId();
			Log.v(TAG, "imsi="+imsi);
		} catch (Exception e) {
		}
		return imsi;
	}

	
	
	public static String getImeiDirectly(Context ctx) {
		String imei = "";
		try {
			TelephonyManager phoneManager = (TelephonyManager) ctx
					.getSystemService(Context.TELEPHONY_SERVICE);
			imei = phoneManager.getDeviceId();
			Log.v(TAG, "imei="+imei);
		} catch (Exception e) {
		}
		return imei;
	}

	
	
	public static String getVersionName(Context ctx) 
	{
		String version="";

		PackageManager packageManager = ctx.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(ctx.getPackageName(),0);
			version = packInfo.versionName;
			Log.v(TAG, "version="+version);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;

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
	

	public static String getOPID(Context ctx){
		// IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
		String PID="";
		String IMSI=getImsiDirectly(ctx);
		try{
			if(IMSI!=null){
				PID=IMSI.substring(0, 5);
			}
		}catch(Exception e){
			//sim 不存在情况
			PID="";
		}
		return PID;
	}


	
	public static String getDevice(){
		String model=android.os.Build.MODEL;
		return URLEncoder.encode(model);
	}

	
	
	public static String getOSVerion(){
		String ver=android.os.Build.VERSION.RELEASE;
		return URLEncoder.encode(ver);
	}


	public static String md5(String string) {
		if(isEmptyStr(string)){
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
	 * 写入SharedPreferences数据
	 * @param String value
	 * */    
	public static void setStringSharedPreferences(Context context,String key,String value){
		SharedPreferences.Editor editor = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}


	/**
	 * 读取SharedPreferences数据
	 * @return String value
	 * */    
	public static String getStringSharedPreferences(Context context,String key, String defaultValue){
		SharedPreferences sharedPref = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return sharedPref.getString(key, defaultValue);
	}



	/**
	 * 写入SharedPreferences数据
	 * @param boolean value
	 * */    
	public static void setBooleanSharedPreferences(Context context,String key,boolean value){
		SharedPreferences.Editor editor = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}


	/**
	 * 读取SharedPreferences数据
	 * @return boolean value
	 * */    
	public static boolean getBooleanSharedPreferences(Context context,String key, boolean defaultValue){
		SharedPreferences sharedPref = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return sharedPref.getBoolean(key, defaultValue);
	}




	/**
	 * 写入SharedPreferences数据
	 * @param int value
	 * */    
	public static void setIntSharedPreferences(Context context,String key,int value){
		SharedPreferences.Editor editor = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE).edit();
		editor.putInt(key, value);
		editor.commit();
	}


	/**
	 * 读取SharedPreferences数据
	 * @return int value
	 * */    
	public static int getIntSharedPreferences(Context context,String key, int defaultValue){
		SharedPreferences sharedPref = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return sharedPref.getInt(key, defaultValue);
	}



	/**
	 * 写入SharedPreferences数据
	 * @param long value
	 * */    
	public static void setLongSharedPreferences(Context context,String key,long value){
		SharedPreferences.Editor editor = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE).edit();
		editor.putLong(key, value);
		editor.commit();
	}


	/**
	 * 读取SharedPreferences数据
	 * @return long value
	 * */    
	public static long getLongSharedPreferences(Context context,String key, long defaultValue){
		SharedPreferences sharedPref = context.getSharedPreferences(AppConfig.SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return sharedPref.getLong(key, defaultValue);
	}




	
	
}
