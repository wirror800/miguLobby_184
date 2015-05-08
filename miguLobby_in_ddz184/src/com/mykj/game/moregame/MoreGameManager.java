package com.mykj.game.moregame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import com.mingyou.accountInfo.LoginInfoManager;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Base64;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class MoreGameManager {

	private static final String TAG = "MoreGameManager";
	private static final String APPVERSIONNAME = "applist.xml";

	private Context mContext;

	//应用列表
	private List<AppVersion> AppConfigList = new ArrayList<AppVersion>();
	
	//下载线程
	private SparseArray<DownloadThread> mDownloadMap;
	
	//奖励列表
	private HashMap<String, String> mReward = new HashMap<String, String>();
	private boolean isLocal = false;
	
	//服务器信息地址
	private String mMainUrl;
	
	
	
	//发送服务器请求id
	public static final int GET_PACKAGE_LIST = 3;   //请求获得应用列表
	public static final int GET_REWARD = 4;         //请求领取奖励
	public static final int DOWNLOAD_FINISH = 5;    //通知服务器下载完成
	
	//请求奖励结果
	public static final int REWARD_SUCCESS = 0;         //请求成功
	public static final int REWARD_SERVER_ERROR = 1;	//服务器端出错
	public static final int REWARD_USERINFO_ERROR = 2;	//用户信息出错
	public static final int REWARD_GAMEINFO_ERROR = 3;	//应用信息出错(如服务器未记录已经下载)
	public static final int REWARD_REWARDER_BEFORE = 4;	//已经领取过

	//解析应用列表结果
	public static final int PARSE_NO_NODE = 0;			//没有子节点
	public static final int PARSE_SUCCESS = 1;			//解析成功
	public static final int PARSE_FAIL = 2;				//解析失败
	
	public MoreGameManager(Context context) {
		mContext = context;
		mDownloadMap = new SparseArray<MoreGameManager.DownloadThread>();
		
		mMainUrl = Util.readFromFile(MoregameConfig.IP_CONFIG_FILE_NAME);
		if(mMainUrl == null || mMainUrl.trim().length() < 1){
			mMainUrl = AppConfig.NEW_HOST;
			
		}
		//获得本地记录的奖励列表
		String rew = Util.readFromFile(MoregameConfig.REWARD_FILENAME());
		Log.i(TAG, "read is " + rew);
		mReward.clear();
		if (rew != null) {
			for (String s : rew.split(",")) {
				if (s.trim().length() > 0) {
					mReward.put(s, "true");
				}
			}
		}
	}

	/** 获取大厅中游戏配置信息 */
	public List<AppVersion> getGamesConfig() {
		return AppConfigList;
	}

	/**
	 * 下载游戏
	 * @param id 游戏id
	 * @param url 下载地址
	 * @param savePath 保存路径
	 * @param md5 校验码
	 * @param handler 回调handle
	 * @param what 列表中的索引
	 */
	public void startDownloadFile(AppVersion game, Handler handler){
		DownloadThread t = mDownloadMap.get(game.getGameId());
		if(t == null || t.isCancelled()){
			DownloadThread nt = new DownloadThread(game, handler);
			nt.start();
			mDownloadMap.put(game.getGameId(), nt);
		}
	}

	public DownloadThread getDownloadThread(int id) {
		return mDownloadMap.get(id);
	}


	/**
	 * 是否本地记录已奖励
	 * @param id 游戏id
	 * @return
	 */
	public boolean isRewardLocal(int id) {
		String s = Integer.toString(id);
		if (mReward.containsKey(s)) {
			return "true".equals(mReward.get(s));
		}
		return false;
	}

	public void setRewardLocal(int id) {
		if (id > 0) {
			String s = Integer.toString(id);
			mReward.put(s, "true");
		}
	}

	public void clearDownloadThread(int id) {
		mDownloadMap.remove(id);
	}

	public void onDestroy() {
		Log.v(TAG, "MoreGameManager is onDestroy");
		if(mDownloadMap != null && mDownloadMap.size() > 0){
			for(int i = 0; i < mDownloadMap.size(); i++){
				mDownloadMap.valueAt(i).cancel();
			}
			mDownloadMap.clear();
		}
		AppConfigList.clear();

	}

	public void onPause() {
		saveLocalRewardToFile();
		/*if(mDownloadMap != null && mDownloadMap.size() > 0){
			for(int i = 0; i < mDownloadMap.size(); i++){
				mDownloadMap.valueAt(i).setPause();
			}
		}*/
	}

	public void onResume(){
		/*if(mDownloadMap != null && mDownloadMap.size() > 0){
			for(int i = 0; i < mDownloadMap.size(); i++){
				mDownloadMap.valueAt(i).setResume();
			}
		}*/
	}

	/**
	 * 将本地奖励记录保存到文件
	 */
	private void saveLocalRewardToFile() {
		if (!isLocal || mReward.size() > 0) {
			Util.deleteFile(new File(MoregameConfig.REWARD_FILENAME()));
			Entry<String, String> et;
			StringBuffer s = new StringBuffer();
			Iterator<Entry<String, String>> it = mReward.entrySet().iterator();
			while (it.hasNext()) {
				et = it.next();
				if ("true".equals(et.getValue())) {
					s.append(",");
					s.append(et.getKey());
				}
			}
			Log.i(TAG, "save = " + s);
			Util.saveToFile(MoregameConfig.REWARD_FILENAME(), s.toString());
		}
	}

	
	/**
	 * 向服务器提交领取奖励
	 * @param gameid 游戏id
	 * @param handler
	 */
	public void commitReward(final int gameid, final Handler handler) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				int answer = REWARD_SERVER_ERROR;
				try {
					String reqUrl = getMoreGameUrl(GET_REWARD, gameid);
					String result = UtilHelper.doGetStatus(reqUrl);
					Log.i(TAG, "reward rlt = " + result);
					if (result != null && result.toString().trim().length() > 0) {
						try {
							answer = Integer.parseInt(result);
						} catch (NumberFormatException e) {
							Log.e(TAG, "获取乐豆返回值非数字");
						}
					}
				} catch (ConnectTimeoutException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

				//通知领取结果
				Message msg = handler.obtainMessage();
				msg.arg1 = answer;
				msg.arg2 = gameid;
				msg.what = MoregameActivity.REWARD_FEEDBACK;
				handler.sendMessage(msg);
			}
		}).start();

	}

	/**
	 * 游戏下载完成报告服务器
	 * @param gameid 游戏id
	 * @param handler
	 */
	public void finishDownloadReport(final int gameid, final Handler handler) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int answer = REWARD_SERVER_ERROR;
				try {
					String reqUrl = getMoreGameUrl(DOWNLOAD_FINISH, gameid);
					String result = UtilHelper.doGetStatus(reqUrl);
					Log.i(TAG, "finish report rlt = " + result);
					if (result != null && result.toString().trim().length() > 0) {
						try {
							answer = Integer.parseInt(result);
						} catch (NumberFormatException e) {
							Log.e(TAG, "下载完成返回值非数字");
						}
					}
				} catch (ConnectTimeoutException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Message msg = handler.obtainMessage();
				msg.arg1 = answer;
				msg.arg2 = gameid;
				msg.what = MoregameActivity.DOWNLOAD_FINISH_BACK;
				handler.sendMessage(msg);
			}
		}).start();
	}

	/**
	 * 获得游戏列表信息
	 * @param handler
	 */
	public void getServerInfo(final Handler handler) {
		new Thread() {
			@Override
			public void run() {
				AppConfigList.clear();
				Message msg = handler.obtainMessage();
				msg.what = MoregameActivity.GET_GAME_VERSION_FAIL;
//				int curDate = Calendar.getInstance().get(Calendar.DATE);
//				int saveDate = Util.getIntSharedPreferences(mContext,
//						"saveDate", -1);
				String wapstr = null;

				/**本地不在缓存*/
				// 优先查找本地xml
//				if ((saveDate >= 0 && curDate - saveDate <= 1 && curDate
//						- saveDate >= 0)
//						|| !Util.isNetworkConnected(mContext)) {
//					wapstr = Util.readFromFile(MoregameConfig.SERVICE_DOWNLOAD_PATH() + "/" + APPVERSIONNAME);
//					if (wapstr != null) {
//						Log.i(TAG, "Local xml parse");
//						isLocal = true;
//						if (parseNotifyXml(wapstr) == PARSE_SUCCESS) {
//							msg.what = MoregameActivity.GET_GAME_VERSION_SUCCESS;
//						} else {
//							// 错误的本地文件
//							Util.deleteFile(new File(MoregameConfig.SERVICE_DOWNLOAD_PATH() + "/" + APPVERSIONNAME));
//							AppConfigList.clear();
//							wapstr = null;
//							isLocal = false;
//						}
//					}
//				}
				// 本地文件不存在，直接从网络获取
				if (!isLocal) {
					Log.v(TAG, "game version config xml read from http");

					String myUrl = getMoreGameUrl(GET_PACKAGE_LIST, -1);
					Log.i(TAG, myUrl);
					try {
						wapstr = UtilHelper.doGetStatus(myUrl);
					} catch (ConnectTimeoutException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						wapstr = null;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						wapstr = null;
					}

					if (wapstr != null && wapstr.trim().length() > 0) {
						Log.i(TAG, "server xml parse");
						Log.i(TAG, wapstr);
						int parseRlt = parseNotifyXml(wapstr);
						if (parseRlt == PARSE_SUCCESS) {
							msg.what = MoregameActivity.GET_GAME_VERSION_SUCCESS;
							
							/**本地不在缓存*/
//							Util.saveToFile(MoregameConfig.SERVICE_DOWNLOAD_PATH() + "/" + APPVERSIONNAME, wapstr);// 将内容保存文件
//							Util.setIntSharedPreferences(mContext, "saveDate",
//									curDate);
						} else if (parseRlt == PARSE_NO_NODE) {
							msg.what = MoregameActivity.GET_GAME_VERSION_NO_NODE;
						}
					}
				}
				handler.sendMessage(msg);
				Log.v(TAG, "serviceHttpGetReqThread appversion xml is finished");
			}
		}.start();
	}

	/**
	 * 解析xml数据获得游戏列表
	 */
	public int parseNotifyXml(String strXml) {
		int isParseSuccess = PARSE_NO_NODE;
		if (strXml == null) {
			return isParseSuccess;
		}
		{
			int startIndex = strXml.indexOf("<?");
			if(startIndex > 0){
				strXml = strXml.substring(startIndex);
			}
		}
		
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
					String tagName = p.getName();
					if (tagName.equals("game")) {
						
						AppVersion gc = new AppVersion(mContext, p);
						String s = Integer.toString(gc.getGameId());

						if (!isLocal) { // 由网络获得已经获取，则把结果保留到本地
							if (gc.isReward()) {
								mReward.put(s, "true");
							} else if ((!gc.isReward())) {
								if (mReward.containsKey(s)) {
									mReward.put(s, "false");
								}
							}
						} else if (isLocal && !gc.isReward()) { // xml文件没更新，则按本地记录为准
							if (mReward.containsKey(s)) {
								if ("true".equals(mReward.get(s)))
									gc.setReward(true);
							}
						}
						if (AppConfigList.isEmpty()) {
							AppConfigList.add(gc);
						} else {
							boolean replace = false;
							for (int i = 0, j = AppConfigList.size(); i < j; i++) {
								int id = AppConfigList.get(i).getGameId();
								if (gc.getGameId() == id) {
									AppConfigList.set(i, gc);
									replace = true;
									break;
								}

							}
							if (!replace) {
								AppConfigList.add(gc);
							}
						}
						isParseSuccess = PARSE_SUCCESS;
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
			Log.v(TAG, "parse xml error");
			isParseSuccess = PARSE_FAIL;
		}
		return isParseSuccess;
	}

	/**
	 * 获得服务器url
	 * @param order 请求命令
	 * @param gameid 游戏id
	 * @return
	 */
	public String getMoreGameUrl(int order, int gameid) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(mMainUrl);
		sb.append("?ac=resource&tid=");
		sb.append(order);
		sb.append("&g=");
		String chlId = AppConfig.clientID + "";

		// <g k=”%d” p=”%d” g=”%d” c=”%s” sc=”%s” t=”%d”>
		String input = String.format(
				"<g k=%1$d p=%2$d g=%3$d c=%4$s sc=%5$s t=%6$d />", 0, 1,
				AppConfig.gameId, chlId + "", AppConfig.childChannelId + "",
				System.currentTimeMillis());
		@SuppressWarnings("deprecation")
		String g = URLEncoder
				.encode(Base64.encode(input.toString().getBytes()));
		sb.append(g);
		sb.append("&tk=");
		try {
			g = URLEncoder.encode(LoginInfoManager.getInstance().getToken(),
					"UTF-8");
			sb.append(g);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (order == GET_REWARD || order == DOWNLOAD_FINISH) {
			sb.append("&downgameid=");
			sb.append(gameid);
		}
		if(order == GET_PACKAGE_LIST){
			sb.append("&ver=");
			sb.append(MoregameConfig.MORE_GAME_VER);
		}
		Log.i(TAG, sb.toString());
		return sb.toString();
	}

	/**
	 * 下载线程内部类
	 * */
	public class DownloadThread extends Thread {
		private String mUrl;
		private String mDownLoadPath;
		private String mMd5;
		private String mDownLoadFileName;
		private int param;
		private boolean cancelled = false;
		private Handler mHandler = null;
		private boolean isPause = false;
		/**
		 * 
		 * @param url
		 *            下载文件url
		 * @param downloadpath
		 *            下载文件保存目录
		 * @param md5
		 *            下载文件校验md5
		 * @param handler
		 *            通知UI线程Handler
		 * @param param
		 *            参数
		 */
		/*public DownloadThread(String url, String downloadpath, String md5,
				Handler handler, int param) {
			mUrl = url;
			mDownLoadPath = downloadpath;
			mDownLoadFileName = Util.getFileNameFromUrl(url);// 从URL中获取文件名
			mMd5 = md5;
			mHandler = handler;
			this.param = param;
		}*/
		
		public DownloadThread(AppVersion game, Handler handler){
			mUrl = game.getDownloadUrl();
			mDownLoadPath = game.getAPKFilePath();
			mDownLoadFileName = game.getAPKFileName();
			mMd5 = game.getDownFileConfigMD5();
			param = game.getGameId();
			mHandler = handler;
		}

		@Override
		public void run() {
			startDownload(mUrl, mDownLoadPath);
		};

		/**
		 * 下载模块
		 */
		private void startDownload(String url, String downloadpath) {
			int res = -1;
			cancelled = false;
			isPause = false;
			Log.v(TAG, "startDownload...");
			File path = new File(downloadpath);
			if (!path.exists()) {
				path.mkdirs();
			}

			File downloadFile = new File(downloadpath + "/" + mDownLoadFileName);
			if (downloadFile.exists()) {
				if (Util.downloadFileMD5Check(downloadFile, mMd5)) {
					// 已经存在 不用下载
					Log.v(TAG, "file is exist,don't download");
					Message msg = mHandler.obtainMessage();
					msg.what = MoregameActivity.SERVICE_DOWNLOAD_SUCCESS;
					mHandler.sendMessage(msg);
					mDownloadMap.delete(param);
					return;
				}else{
					downloadFile.delete();
				}
			}

			String downLoadFileTmpName = mDownLoadFileName + ".tmp"; // 设置下载的临时文件名
			String downLoadFileTmpPath = downloadpath + "/"
					+ downLoadFileTmpName;

			File tmpFile = new File(downLoadFileTmpPath);

			try {
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
					long length = entity.getContentLength();// 请求文件长度

					InputStream inputStream = entity.getContent();
					byte[] b = new byte[1024];
					int readedLength = -1;

					OutputStream outputStream = new FileOutputStream(tmpFile,
							true);

					long percentile = (length + startPosition) / 20; // 文件每下载5%的长度
					int rate = 0;
					int count = 0;

					while (((readedLength = inputStream.read(b)) != -1)) {
						
						if (cancelled) {
							inputStream.close();
							outputStream.close();
							if (mHandler != null) {
								Message msg = mHandler.obtainMessage();
								msg.what = MoregameActivity.SERVICE_DOWNLOAD_CANCLE;
								Bundle bundle = new Bundle();
								bundle.putInt("gameId", param);
								bundle.putInt("rate", rate);
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}
							//mDownloadMap.delete(param);
							return;
						}
						synchronized (this) {
							if(isPause){
								inputStream.close();
								outputStream.close();
								return;
							}
						}
						
						outputStream.write(b, 0, readedLength);
						startPosition += readedLength;

						if (startPosition >= percentile) // 每下载5%，计算进度条
						{
							count = (int) (startPosition / percentile) * 5;
							Log.v(TAG, "count=" + count);
							rate += count;

							startPosition = 0;
							count = 0;

							if (mHandler != null) {
								Message msg = mHandler.obtainMessage();
								msg.what = MoregameActivity.UPDATE_DOWNLOAD_PROGRESS;
								Bundle bundle = new Bundle();
								bundle.putInt("gameId", param);
								bundle.putInt("rate", rate);
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}

							Log.v(TAG, "文件已下载" + rate + "%");

						}
					}
					// 设置进度条100%
					if (mHandler != null) {
						Message msg = mHandler.obtainMessage();
						msg.what = MoregameActivity.UPDATE_DOWNLOAD_PROGRESS;
						Bundle bundle = new Bundle();
						bundle.putInt("gameId", param);
						bundle.putInt("rate", 100);
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}

					inputStream.close();
					outputStream.close();

					// 下载文件MD5检测
					if (Util.downloadFileMD5Check(tmpFile, mMd5)) {
						Log.v(TAG, "download file md5 check success");

						tmpFile.renameTo(downloadFile);
						res = 0;
					} else {
						Log.e(TAG, "download file md5 check fail");
						tmpFile.delete();
						res = 1;
					}

				}
			} catch (ClientProtocolException e) {
				res = 2;
				Log.e(TAG, "file download fail");
			} catch (IOException e) {
				res = 3;
				Log.e(TAG, "file download fail");
			} catch (IllegalArgumentException e){
				res = 4;
				Log.e(TAG, "file download fail");
			} catch (Exception e){
				res = 5;
				Log.e(TAG, "file download fail");
			}
			if (mHandler != null) {
				Message msg = mHandler.obtainMessage();
				String str = downloadFile.getPath();
				Bundle bundle = new Bundle();
				bundle.putInt("gameId", param);
				bundle.putString("filepath", str);
				msg.setData(bundle);
				if (res == 0) {
					msg.what = MoregameActivity.SERVICE_DOWNLOAD_SUCCESS;
				} else {
					msg.what = MoregameActivity.SERVICE_DOWNLOAD_FAIL;

				}
				cancelled = true;
				mHandler.sendMessage(msg);
				mDownloadMap.delete(param);
			}
		}

		/**
		 * 取消下载
		 */
		public void cancel() {

			cancelled = true;
		}

		/**
		 * 是否已被取消
		 * 
		 * @return
		 */
		public boolean isCancelled() {
			return cancelled;
		}
		
		public boolean isPause(){
			return isPause;
		}
		
		public void setPause(){
			isPause = true;
		}
		public void setResume(){
			if(isPause){
				isPause = false;
				synchronized (this) {
					if(!isAlive())
						run();
				}
				
			}
		}
	}
}
