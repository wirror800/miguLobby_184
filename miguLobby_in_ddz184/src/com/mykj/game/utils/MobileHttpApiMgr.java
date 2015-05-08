package com.mykj.game.utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 *  移动侧HTTP接口管理类（主要是移动的接口）
 *  此类实现移动侧HTTP接口的访问封装：移动网游接口，白名单地址等
 * @author FWQ
 *
 */
public class MobileHttpApiMgr {
	private static String TAG = "MobileHttpApiMgr";
	private Context context;
	private static MobileHttpApiMgr instance;
	
	
	/**记录网游接口地址放回的用户伪码 userId*/
	private String onlineGame_userId = "";
	private String onlineGame_key = "";
	private int cpServiceId;
	
	
	/**白名单地址 默认值*/
	private String WHITENAME_CMWAP_URL = "http://g.10086.cn/home/interface/AutoRegLogin.php?app=qbdx";
	
	
	private MobileHttpApiMgr(){
		
	}
	
	/**
	 * 单例
	 * @return
	 */
	public static MobileHttpApiMgr getInstance(){
		if(instance == null){
			instance = new MobileHttpApiMgr();
		}
		return instance;
	}
	
	
	
	
	/**
	 * 开启管理器：（在程序启动时调用初始化）
	 * 将执行如下流程:
	 * 1:从记录中读取 http://mingyou.cmgame.com/cmwapgame.aspx?cid=上的配置数据，没有记录则重新下载
	 * 2:解析出白名单地址并记录，供外部调用getWhitenameCmwapUrl()获取
	 * 3：解析出网游接口地址，并连接获取移动网游用户伪码，供外部调用getOnlineGameUserId()获取
	 * 4：定时访问网游接口地址，更新用户伪码
	 * @return
	 */
	public boolean start(Context _context,String cid){
		context = _context;
		if(context == null){
			throw new NullPointerException("MobileHttpApiMgr start context is NULL");
		}
		refreshIpArray_OnlineGame(cid);
		
		return true;
	}
	
	
	/**
	 * 获得移动CMWAP购买的用户伪码
	 * <br>如果获取到的值是null或""，则使用短信支付
	 * @return
	 */
	public String getOnlineGameUserId(){
		return onlineGame_userId;
	}
	
	public String getOnlineGameKey(){
		return onlineGame_key;
	}
	
	public int getServiceId(){
		return cpServiceId;
	}
	
	
	
	/**
	 * 获得用于获取白名单的地址
	 * @return
	 */
	public String getWhitenameCmwapUrl(){
		return WHITENAME_CMWAP_URL;
	}
	
	
	/***
	 * @Title: cbTimerRefreshOnlineGameKey
	 * @Description: 获取IP列表并得到移动用户伪码，以及移动用户move_mobile_key
	 * @param cid
	 * @version: 2012-10-31 下午08:07:07
	 */
	private void refreshIpArray_OnlineGame(final String cid){
		//获得取配置的
		String onlineGameNewtUrl = getConfigOnlineGameNew(cid);
		//获取配置的地址
		String onlineGameNewContent = Util.getConfigXmlByHttp(onlineGameNewtUrl,3);
		if(Util.isEmptyStr(onlineGameNewContent)){
			//没有获得配置，中断处理
			return;
		}
		//解析出报名单地址
		updateWhiteNameWapPortUrl(onlineGameNewContent);

		//解析新结构网游接口地址
		
		ArrayList<LoginOnlineGame> lists = getOnlineGameUrls(onlineGameNewContent); 
		if(lists.isEmpty()){
			return;
		}
		
		//定时请求
		timerRequestMoblieKey(lists);
	}


	
	/**
	 * 定时刷新网游接口地址
	 * @param urls
	 */
	private void timerRequestMoblieKey(final ArrayList<LoginOnlineGame> lists){
		if(lists.isEmpty()){
			return;
		}
		Timer time = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				for(LoginOnlineGame item:lists){
					//解析网游接口地址，获得购买使用的key(移动用户move_mobile_key)
					boolean res=parseOnlineGameData(item);
					if(res){
						break;
					}
				}
			}
		};
		long delaytime = 3*60*60*1000;
		time.schedule(task,0,delaytime);//每3小时刷新一次
	}

	/****
	 * @Title: PropKeyOnData
	 * @Description: 解析移动返回数据得到移动用户伪码，以及移动伪码
	 * @param content
	 * @version: 2012-11-1 下午12:30:51
	 */
	private boolean parseOnlineGameData(LoginOnlineGame arg){
		boolean res=false;
		
		String content=null;
		
		if(Util.isCMWap(context)){
			//只有在CMWAP时才请求
			content = Util.getConfigXmlByHttp(arg.mUrl,3);//自动重试3次
		}
		
		if(Util.isEmptyStr(content)){
			return false;
		}
		
		try {
			int start=content.indexOf("userId=")+7;
			int end=content.indexOf("key=");

			String move_userId = content.substring(start, end).trim();
			if(move_userId!=null && move_userId.length()>0){
				onlineGame_userId = move_userId;
				
				cpServiceId=arg.Id;

			}

			String move_mobile_key = content.substring(end+4).trim();
			if(move_mobile_key!=null && move_mobile_key.length()>2){  //可能NULL字符串
				onlineGame_key = move_mobile_key;
			}
			res=true;
		
		} catch (Exception e) {
			return false;
		} 
		
		return res;
	}

	
	
	
	/**
	 * 组装获取网游接口地址的url
	 * @param cid
	 * @return
	 */
	private String getConfigOnlineGameNew(String cid){
		StringBuffer sb = new StringBuffer();
		sb.append("http://qpwap.cmgame.com/get_curllist.php");
		sb.append("?");
		sb.append("gameid=");
		sb.append(AppConfig.gameId);
		sb.append("&");
		sb.append("cid=");
		sb.append(cid);
		return sb.toString();
	}



	/**
	 * 解析出CMWAP白名单地址，并记录
	 * @param onlineGameNewContent
	 */
	private void updateWhiteNameWapPortUrl(String onlineGameNewContent)
	{
		try { 
			JSONObject jSONObject=new JSONObject(onlineGameNewContent);
			String url=String.valueOf(jSONObject.get("lgurl"));
			if(!Util.isEmptyStr(url)){
				WHITENAME_CMWAP_URL= url; //更新白名单地址
			}
		} catch (Exception e) {
			Log.e(TAG, "解析白名单地址出错，请检查web配置");
		} 
	}

	/**
	 * 解析新格式的网游接口地址
	 * 访问http://qpwap.cmgame.com/get_curllist.php返回的数据
	 * @param onlineGameNewContent
	 * @return
	 */
	private ArrayList<LoginOnlineGame> getOnlineGameUrls(String onlineGameNewContent){
		try { 
			JSONObject jSONObject=new JSONObject(onlineGameNewContent);
			JSONArray  jsonArray=(JSONArray)jSONObject.get("curllist");
			if(jsonArray==null){
				return null;
			}
			ArrayList<LoginOnlineGame> list = new ArrayList<LoginOnlineGame>();
			for(int i=0;i<jsonArray.length();i++){
				JSONObject mjSONObject=jsonArray.getJSONObject(i);
				LoginOnlineGame item=new LoginOnlineGame();
				String url =String.valueOf(mjSONObject.get("url"));
				item.mUrl=url;
				try{
					int id=mjSONObject.getInt("id");
					item.Id=id;
		
				}catch(JSONException e){
					
				}
				list.add(item);
			}
			return  list;
		}catch (Exception e) {
			Log.e(TAG, "解析网游接口地址出错，请检查web配置");
		} 
		return null;
	}

	
	
	private class LoginOnlineGame{
		String mUrl;
		int Id;
	}
	
	

}
