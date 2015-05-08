package com.mykj.game.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Xml;

import com.mingyou.login.struc.VersionInfo;
import com.mykj.andr.model.RoomInfoEx;
import com.mykj.andr.ui.CustomActivity;
import com.mykj.game.FiexedViewHelper;

/**
 * 
 * @author Administrator
 * @author FWQ 增加控制是否验证版本的控制变量 20120830
 */
public class AppConfig extends debug.IP_CONFIG_FILE {


	/************************以下为项目配置开关*******************************/
	public static String buildTime="";
	/**版本描述信息只能手动修改，每次新版发布前请更新*/
	public static String versionInfo="更新日志：\n1. 高清游戏体验；\n2. 癞子玩法，加入百搭牌，炸弹多多；\n3. 小兵玩法加入攻防，新鲜刺激；\n4. 体验优化，bug修复；";
	
	/** 调试开关 */
	public static boolean debug = false;   //支持配置文件
	
	public static boolean all_in_lobby=false;  //是否发布移动版本，表现为loading 界面风给为移动要求风格
	
	public static String pay_version = "6.2"; //支付组件版本号
	/**
	 * * @param platId
	 *            :平台ID 0：名游 2：百度 3：移动
	 */
	public static short plat_id = 3; //平台id,支付的扩展数据需要	

	
	/**标示是否从SD卡读取渠道号---注意：只是在给下载站点打包时为true*/
	public static final boolean isReadChannelFromSD = false;
	
	
	
	
	public static String imgUrl = "http://image.139game.net/ddz/android/shop/";
	
	/**************wap.139game.net******************/
	public static final String WAP_HOST	= "http://wap.139game.net";  
	//public static final String WAP_HOST	= "http://m.139game.com";  
	
	public static final String NEW_HOST	= WAP_HOST+"/follow.php";  //现网更多游戏下载地址
	/**************wap.139game.net******************/
	

	/************update*******************/
	public static final String UPDATE="http://update.139game.com";
	public static final String UPDATE1="http://update1.139game.com";
	
	public static final String updateUrl = UPDATE+"/android/androidconfig.php";
	
	public static final String onlineServer=UPDATE+"/android/customer.php";  //获取在线客服地址
	
	/**android VIP功能相关***/
	public static final String VIPHOST=UPDATE+"/android/vip.php";
	/**分享内容地址*/
	public static final String SHARE_DOWNPATH =UPDATE+"/android/package.php";
	/**分享内容地址1*/
	public static final String SHARE_DOWNPATH1 =UPDATE1+"/android/package.php";
	
	/************update*******************/

	
	/***************qpwap.cmgame.com*****************/
	public static final String	HOST	="http://qpwap.cmgame.com";           //现网排行榜地址
	/** 短信购买后请求url*/
	public static final String SMS_URL = HOST+"/shop/confirm_pay.php";
	
	/**请求绑定服务去地址*/
	public static final String  REQ_BIND_URL=HOST+"/get_third_entry.php";
	
	/**比赛详情地址*/
	public static final String MATCHINFO=HOST+"/redir.aspx";
	
	/**话费券详情地址*/
	public static final String HUAFEI_INFO=HOST+"/help/huahui.aspx";
	
	/** 服务中心路径 */
	public static final String	SERVER_PATH	= HOST+"/help/ServiceConfig.aspx";
	/***************qpwap.cmgame.com*****************/
	
	/*********************api.139game.com***********************/
	public static final String API="http://api.139game.com";
	
	/**合成图片下载地址*/
	public static final String MIXICON_PATH=API+"/android/shop";

	/** 推广分红主url */
	public static final String WEIXIN_SHARE = API+"/spread/index.php";

    public static final String CMCC_SDK_URL = API+"/pay/index.php";  //"http://192.168.6.27:803/pay/index.php"; API+"/pay/index.php"
	/**
	 * 头像下载地址
	 */
	private static final String GAME_NAME = "ddz";
	public static String HEAD_ICON_URL = API + "/android/shop/avatar/"+GAME_NAME+"/";
	/*********************m.139game.com***********************/
	public static final String M_139="http://m.139game.com";
	/**分享规则*/
	public static final String SHARE_RULEPATH =M_139+"/html/ddz/rule.html";
	
	/*********************m.139game.com***********************/


	
	/**约战邀请推广地址*/
	public static final String DOWNLOADPATH="http://g.10086.cn/gamecms/go/qpds";
	
	/**修改账号密码*/
	public static final String MODIFY_URL = "http://game.10086.cn/home/do.php?ac=lostpasswd";
	/*************http 配置地址******************/
	
	
	/**分渠道用户支付行为统计*/
	//public static final String PLAY_ACTION="http://192.168.5.61:803/pay/index.php";
	public static final String PLAY_ACTION="http://api.139game.com/pay/index.php";
	
	/**美女猜猜猜钻石购买*/
	public static String MMVIDEO_DIAMOND_URL="http://qpgame.cmgame.com/shop/meinv/configdownload.php";
	
	
	public static String NEW_GETTICKET_URL="http://wap.139game.net/activity.php?ac=yuanbao";
	/************************以下为游戏常量定义*******************************/

	
	
	// 游戏ID定义
	/** 斗地主 100*/
	public static final int GAMEID_DDZ = 100;

	/** 围棋 39*/
	public static final int GAMEID_WQ = 39;

	/** 象棋 28*/
	public static final int GAMEID_XQ = 28;

	/** 五子棋 29*/
	public static final int GAMEID_WZQ = 29;

	/** 干瞪眼 11 */
	public static final int GAMEID_GDY = 11;

	/** 国标麻将 121 */
	public static final int GAMEID_GBMJ = 121;

	/**德州扑克44**/
	public static final int GAMEID_TEXAS=44;

	public static final String ZONE_VER="1.8.4";

	/**pref文件名定义*/
	public static final String SHARED_PREFERENCES = "GameZone";

	/**消息推送URL组成关键参数*/
	public static final String CONFIG_MSG="method=msg";

	public static final String CONFIG_RES="cmd=resource";
	
	/** 下载文件的保存文件夹 **/
	public static final String DOWNLOAD_FOLDER = "/.mingyouGames";
	
	public static final String THEME_PATH = DOWNLOAD_FOLDER+"/theme";
	
	public static final String ICON_PATH = "/icons";
	
	public static final String COMMENDATION ="/Pictures";
	
	public static final String LOTTERYBMP_PATH = "/lotterybmp";

	// CID后缀   CID去除 使用渠道号代替
	//private static final String MOBILE_CODE_TAIL = "02ANDROID1";

    public static final String MM_VIDEO_APPID="500020";

	/************************以下为游戏配置变量定义*******************************/

	private static final String TAG = "AppConfig";

	/**是否正在白名单绑定*/ 
	private static boolean isBinding = false;

	/**用户白名单token*/ 
	private static String whiteNameToken = null;

	private static RoomInfoEx mRoomInfoEx=null;
	
	/**提供游戏全局使用context,如果当前界面没有传入context*/
	public static Context mContext;   
	
	/**服务器推荐的道具ID**/
	public static int propId=64;
	/**小钱包道具id*/
	public static int smallMoneyPkgPropId = 10002;
	
	/**快捷购买二次确认
	 * false 不需要二次确认
	 * true  需要二次确认
	 * */
	public static boolean isConfirmon=false;
    
	/**是否显示购买弹出框的取消按钮*/
	public static boolean isShowCancel=true;
   	
	
	/**电量*/
	public static int phonePower=0;
	/**信号量*/
	public static int phoneSignal=0;
	/**网络类型  wifi is 1*/
	public static int phoneApnType=0;
	

	public static String BIND_URL = "http://192.168.1.186:18765/user.login";

	/**消息推送URL*/
	public static  String MSG_URL = "http://push.139game.com/request/api.php";
	
	/** 游戏渠道号 **/
	public static String channelId = "";

	/** 子渠道号 **/
	public static String childChannelId = "";

	/** 渠道伪码 **/
	public static String fid = "";

	


	/** 游戏ID, 斗地主100，围棋39，象棋28， 五子棋29 干瞪眼12 **/
	public static final int gameId = GAMEID_DDZ;


	// 客户端标示，用于登录，注册 的后台统计，由channelId生成
	public static  int clientID = 8080;

	/**
	 * CID: 格式   改为渠道号 20131226
	 */
	public static String CID = "";//"000" + MOBILE_CODE_TAIL;  //使用占位 "000"

	/**登录送数据*/
	public static String giftPack = "";
	
	/**注册送数据*/
	public static String reggiftPack = "";
	
	/**抽奖送数据*/
	public static String luckyDrawPack = "";

	
	public static List<String> personInfoList = new ArrayList<String>();

	/**游戏背景图片地址*/
	public static String bgPath=null;

	/**用户推广码*/
	public static String spKey;

	/**************************
	 * 标识背包显示标记
	 **************************/
	/**
	 * 规定顺序 活动&背包&消息
	 * 0 代表显示动画或new标记
	 * 1代表不显示动画或new标记
	 */
	public static String DEFAULT_TAG="0&0&0";
	
	

	//public static String mBindUrl;
	
	public static final int ACTION_LOTTERY  =1 ;// 抽奖【充】                       
	public static final int ACTION_ZONE  =2 ;// 分区【充】                       
	public static final int ACTION_RO0M  =3 ;// 房间【＋】                       
	public static final int ACTION_BUY_NO  =4 ;// 弹出的购买窗口 - 【关闭】        
	public static final int ACTION_BUY_YES  =5 ;// 弹出的购买窗口 - 【购买】        
	public static final int ACTION_BUY_MORE  =6 ;// 弹出的购买窗口 - 【更多商品】    
	public static final int ACTION_MARKET  =7 ;// 商城列表 - 【购买】              
	public static final int ACTION_MARKET_CLOSE  =8 ;// 商城购买弹窗 - 【关闭】          
	public static final int ACTION_MARKET_NO  =9 ;// 商城购买弹窗 - 【取消】          
	public static final int ACTION_MARKET_YES  =10;// 商城购买弹窗 - 【确定】          
	/***************以下为全局方法区***************************/
	
	/**
	 * 分区talking data 数据统计
	 * @param str
	 */
	public static  void talkingData(int action,int propid,int signtype,String orderno){ 
		String parm=countUsersPlayAction(mContext,action,propid,signtype,orderno);
		try {
			parm = URLEncoder.encode(Base64.encode(parm.getBytes()),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			parm=null;
		}

		String url=AppConfig.PLAY_ACTION+"?method=log&xmldata="+parm;
		((CustomActivity)mContext).doTalkingData(url);

	}
	

	
	/**
	 * 
	 * @param context
	 * @return
	 */
	private static  String countUsersPlayAction(Context context,int action,
			int propid,int signtype,String orderno) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("utf-8", null);
			serializer.startTag(null, "P");
			
			serializer.attribute(null, "cid",channelId);
			serializer.attribute(null, "scid",childChannelId);
			serializer.attribute(null, "gid", ""+gameId);
			serializer.attribute(null, "op",""+UtilHelper.getMobileCardType(context));
			serializer.attribute(null, "os", "02");  //操作系统 02安卓
			int usrId = FiexedViewHelper.getInstance().getUserId();
			int playid=Util.getIntSharedPreferences(context, "gameType" + usrId,
					FiexedViewHelper.GAME_TYPE_UNKNOW);
			serializer.attribute(null, "playid", 
					""+playid);
			serializer.attribute(null, "pver",pay_version);
			serializer.attribute(null, "uid",""+usrId);
			serializer.attribute(null, "versionName",Util.getVersionName(context));
			serializer.attribute(null, "entry",""+action);
			serializer.attribute(null, "shopid",""+propid);  
			
			
			serializer.attribute(null, "signtype",""+signtype); 
			serializer.attribute(null, "orderno",orderno); 
			
			serializer.endTag(null, "P");
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
	
	

	public static final int HALL_TAG = 0;
	public static final int GAME_TAG = 1;
	public static final int MOBILE_TAG = 2;
	//jason2013.01.26.11.13增加启动类型
	private static int launchType = GAME_TAG;
	/**
	 * 获取游戏启动类型
	 * @return
	 * HALL_TAG = 0
	 * GAME_TAG = 1
	 */
	public static int getLaunchType() {
		return launchType;
	}

	/**
	 * 设置游戏启动类型
	 * @param type
	 * HALL_TAG = 0
	 * GAME_TAG = 1
	 */
	public static void setLaunchType(int type) {
		launchType = type;
	}




	public static RoomInfoEx getRoomInfoEx(){
		return mRoomInfoEx;
	}


	public static void setRoomInfoEx(RoomInfoEx roomInfo){
		mRoomInfoEx=roomInfo;
	}
	
	/**
	 * 根据channelId 初始化clientID，CID
	 * 
	 * @author FWQ 20121127
	 */
	public static void initClientCID() {
		if (Util.isEmptyStr(channelId) || channelId.length()>4) {
			channelId = "8080";
		}
		
		try {
			clientID = Integer.parseInt(channelId);
		} catch (Exception e) {
		}
		
		CID = channelId;
	}



	private static  boolean isCmccOpen=false; 
    /**
     * 移动支付SDK是否打开
     * @return
     */
//	public static boolean isOpenPayByCmccSdk() {
//		return isCmccOpen;
//	}
	
	public static void setCmccSwitch(Context context,int config){
		Util.setIntSharedPreferences(context, "CMCC_SDK", config);
	}
	
	public static void initCmccSwitch(Context context){
		int res=Util.getIntSharedPreferences(context, "CMCC_SDK", 1);
		isCmccOpen= (res==1?true:false);
//		isCmccOpen = true;
	}

	
	private static VersionInfo updateInfo;
	
	public static VersionInfo getVersionInfo() {
		return updateInfo;
	}
	
	public static void setVersionInfo(VersionInfo ver) {
		updateInfo=ver;
	}
	
	
	
	/**
	 * 初始化渠道号
	 * @param context
	 */
	public static void readChannelId(Context context){
		try {
			Properties pro = new Properties();
			InputStream is = context.getAssets().open("channel.properties");
			pro.load(is);
			AppConfig.channelId = pro.getProperty("channelId");
			AppConfig.childChannelId = pro.getProperty("childChannelId");
			is.close();
		} catch (Exception e) {
			Log.e(TAG, "channelId read error");
		}
	}
	

	/**
	 * 读取版本等相关信息
	 * @param context
	 */
	public static void loadVersion(Context context){
		if(context == null){
			throw new NullPointerException("AppConfig.loadVersion context is null");
		}
		try {
			Properties pro = new Properties();
			InputStream is = context.getAssets().open("channel.properties");
			pro.load(is);
			String tmpVersionName = pro.getProperty("versionName");
			String tmpbuildTime = pro.getProperty("buildTime");
			
			AppConfig.buildTime = new String(tmpbuildTime.getBytes("ISO-8859-1"),"UTF-8");
			is.close();
			is = null;
		} catch (Exception e) {
			Log.e(TAG, "AppConfig.loadVersion have Exception e = "+e.getMessage());
		}
		android.util.Log.d(TAG, "versionName = "+Util.getVersionName(context));
		android.util.Log.d(TAG, "buildTime = "+AppConfig.buildTime);
		android.util.Log.d(TAG, "versionInfo ="+AppConfig.versionInfo);
	}
	

	/**
	 * 从配置文件读取IP/端口以及是否可以链接外网的标识
	 * 
	 */
	public static void readIpPortFormConfig() {
		String filePath = Util.getSdcardPath();
		File file=new File(filePath,IP_CONFIG_FILE);
		if(file.exists()){
			try {
				Properties pro = new Properties();
				InputStream is=new FileInputStream(file);
				pro.load(is);
				connIP=pro.getProperty("ip");	

				String port=pro.getProperty("port");
				if(!Util.isEmptyStr(port)){
					connPort=Integer.parseInt(port);
				}

				String isout=pro.getProperty("isout");
				if(!Util.isEmptyStr(isout)){
					boolean isOuterNet=Boolean.parseBoolean(isout);
					AppConfig.setIsOuterNet(isOuterNet);
				}
				AppConfig.BIND_URL=pro.getProperty("bindUrl");

				String debug=pro.getProperty("debug");
				if(!Util.isEmptyStr(debug)){
					AppConfig.debug=Boolean.parseBoolean(pro.getProperty("debug"));	
				}
				
				String msg_url=pro.getProperty("msg_url");
				if(!Util.isEmptyStr(msg_url)){
					AppConfig.MSG_URL=pro.getProperty("msg_url");	
				}
				
				String head_url=pro.getProperty("head_url");
				if(!Util.isEmptyStr(head_url)){
					AppConfig.HEAD_ICON_URL = head_url;
				}
				String meinvConfig=pro.getProperty("meinvconfig");
				if(!Util.isEmptyStr(meinvConfig)){
					AppConfig.MMVIDEO_DIAMOND_URL = meinvConfig;
				}
				is.close();
			} catch (Exception e) {

			}
		}
		
	}

	
	
	/**登录送弹出配置, 1首次弹，2次次弹*/
	public static final byte LOGIN_GIFT_SHOW_FIRSTTIME = 1;
	public static final byte LOGIN_GIFT_SHOW_EVERYTIME = 2;
	public static byte LoginGiftSwitch = LOGIN_GIFT_SHOW_EVERYTIME;
	/**是否已接收登录送弹出开关*/
	public static boolean isReceiveLoginGiftSwitch = false;
	
	/**标识是否已接收消息 */
	public static boolean isReceive = false ;
	
	public static String UNIT;
	
	// 渠道鉴权用户名VALUE
	public static String gamePlayerUserId = "";

	// 渠道鉴权密码VALUE
	public static String gamePlayerPwd = "";

	// 渠道编码VALUE
	public static String gamePlayerChannelCode = "";

	// 接口地址VALUE
	public static String gamePlayerBaseUrl = "";
	
	// 配置文件名称
	public static final String GAME_PLAYER_PROPERTIES_FILE_NAME = "gamePlayerProperties.properties";

	// 渠道鉴权用户名KEY
	public static final String GAME_PLAYER_USERID = "authUserid";

	// 渠道鉴权密码KEY
	public static final String GAME_PLAYER_PWD = "authPwd";

	// 渠道编码KEY
	public static final String GAME_PLAYER_CHANNELCODE = "channelCode";

	// 接口地址KEY
	public static final String GAME_PLAYER_BASEURL = "baseUrl";
	
	// 接口地址KEY
	public static final String GAME_PLAYER_ENABLE = "isGamePlayerEnable";
	
	/**
	 * 加载gamePlayerProperties.properties文件
	 * 主要配置游戏玩家参数
	 */
	public static void loadGamePlayerProperties(Context context) {
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
		}
	}
	

}
