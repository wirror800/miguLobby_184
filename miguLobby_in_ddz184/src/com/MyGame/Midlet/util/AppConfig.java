package com.MyGame.Midlet.util;

public class AppConfig
{
	/**游戏配置和广告配置URL*/
	public static  String CONFIG_URL ="http://update.139game.com/android/androidconfig.php?";  

    /**游戏详情配置URL*/
	public static  String GAMEINFO_URL="http://api.139game.com/android/subarea.php?";
	
	/**大厅消息推送URL*/
	public static  String MSG_URL = "http://push.139game.com/request/api.php?";
	
	/**个人用户信息URL*/
	public static  String PERSIONINFO_URL = "http://api.139game.net/user.php?";
	
	/**用户反馈URL*/
	public static String FEEDBACK_URL = "http://api.139game.com/imail/do.php?";
	
	public static final long DEFAULT_FQCY=24*60*60*1000; //1天;
	public static long WATCHDOG_DELAY=0; 
	public static boolean Cooldown_MIN=false; 
	
    public static final String CONFIG_CMD_LOBBY = "cmd=lobby";
	public static final String CONFIG_CMD_GAMES = "cmd=games";
	public static final String CONFIG_CMD_ADV = "cmd=ad";
	public static final String CONFIG_MSG="method=msg";
	public static final String CONFIG_CUSTOM="method=custom";
	
	public static final String PARENT_PATH= "/.miguGames";
	public static final String ICONS_PATH = PARENT_PATH + "/icons";	
	public static final String APKS_PATH = PARENT_PATH + "/apks";
	public static final String ADS_PATH = PARENT_PATH + "/ads_Ver";
	public static final String NAMEBMP_PATH = PARENT_PATH + "/namebmps";
	public static final String SERVICE_DOWNLOAD_PATH = PARENT_PATH + "/download";
	public static final String GAMEINFO_PATH = PARENT_PATH + "/gameinfo";

	
	public static final String PROPERTIES_FILE_NAME = "channel.properties";
	public static final String PROPERTY_CHANNEL_ID = "channelId";
	public static final String PROPERTY_FID = "fid";
	public static final String PROPERTY_CHILD_CHANNEL_ID = "childChannelId";
	public static final String DEFAULT_CHANNEL_ID = "080";
	public static final String DEFAULT_FID = "";
	public static final String DEFAULT_CHILD_CHANNEL_ID = "0000";


	public static final String SHARED_PREFERENCES = "gamelobby";
	
	public static String Token = "";
	public static String IMSI = "";  

	/** 渠道号 */
	public  static String channelId = "";
	/** 客户端标识 */
	public  static String clientId = "";
	/** 渠道伪码 */
	public static String fid = "";
	/** 子渠道号 */
	public static String childChannelId = "";

	/** 手机型号 */
	public  static String m = "";

	/**操作系统版本*/
	public  static String v = "";
		
	/**当sd卡上的剩余空间小于这个数时，停止下载*/
	public static final long WARNING_MEMORY_SIZE = 1024*1024*1;
	
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

	// 渠道鉴权用户名VALUE
	public static String gamePlayerUserId = "";

	// 渠道鉴权密码VALUE
	public static String gamePlayerPwd = "";

	// 渠道编码VALUE
	public static String gamePlayerChannelCode = "";

	// 接口地址VALUE
	public static String gamePlayerBaseUrl = "";
	
	// 游戏玩家功能是否可用
	public static boolean isGamePlayerEnable;
	   /**
	    * gameid,版本号
	    * 
	    */
	public static final String[][] LocalGameConfigs={{"100","1.8.4",".DDZ_Local_Activity"}};

}
