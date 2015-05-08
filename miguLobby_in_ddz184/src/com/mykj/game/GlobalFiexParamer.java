package com.mykj.game;

import java.io.IOException;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.community.Community;
import com.mingyou.community.MUserInfo;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.minyou.android.net.HttpConnector;
import com.minyou.android.net.IRequest;
import com.minyou.android.net.NetService;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.PopularizeSPKey;
import com.mykj.andr.model.SystemPopMsg;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.fragment.AmusementFragment;
import com.mykj.andr.ui.fragment.CardZoneFragment;
import com.mykj.andr.ui.fragment.ChallengeFragment;
import com.mykj.andr.ui.fragment.Cocos2dxFragment;
import com.mykj.andr.ui.fragment.FreeRoomInfoFragment;
import com.mykj.andr.ui.fragment.LoadingFragment;
import com.mykj.andr.ui.fragment.LoginViewFragment;
import com.mykj.andr.ui.fragment.LogonViewFragment;
import com.mykj.andr.ui.fragment.NodifyPasswordFragment;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;

public class GlobalFiexParamer {


	protected static int curFragmentFlag = -1;  //当前fragment标示

	protected FragmentActivity mAct;

	//-------------------fragment tag---------------------------------------
	/** 登录模块fragment **/
	public static final int LOGIN_VIEW = 0;

	/** 新分区fragment **/
	public static final int CARDZONE_VIEW = 1;

	/** Cocos2d-x游戏fragment **/
	public static final int COCOS2DX_VIEW = 2;

	/** 修改账号密码fragment **/
	public static final int NODIFY_ACCOUNT_VIEW = 3;

	public static final int LOGON_VIEW = 4;

	public static final int LOADING_VIEW = 5;

	public static final int FREE_ROOM = 6;

	public static final int CHALLENGE_ROOM = 7;

	public static final int AMUSEMENT_ROOM = 8;

	public static final int FRAGMENT_START = LOGIN_VIEW;
	
	public static final int FRAGMENT_END = AMUSEMENT_ROOM;

	//-----------------------协议-------------------------------
	/** 4.4.1. 登录操作相关 主协议 */
	protected static final short MDM_LOGIN = 12;

	protected static final short MSUB_MULTI_LOGIN = 2; // 子协议

	protected static final short MSUB_SYSMSG_POP_MSG = 105;

	protected static final short MSUB_SYSMSG_POP_MODE_MSG = 106;  //登录送开关，每天1次或者次次
	
	protected static final short MSUB_SYSMSG_REQUEST = 101;

	/** 4.4.1.9. 请求切换游戏 **/
	protected static final short MSUB_CMD_CHANGE_GAME_V2_REQ = 9;

	/** 切换游戏返回结果:失败/成功：返回 **/
	protected static final short MSUB_CMD_CHANGE_GAME_V2_INFO = 10;


	//--------------------handler what-------------------------------------
	protected static final int HANDLER_CHANGE_GAME_ERROR = 101;

	protected static final int HANDLER_REQUEST_CARD_ZONE_DATA = 214; // 请求分区数据

	/** 异地登陆协议handler ***/
	protected static final int HANDLER_OTHER_ADDRESS_LOGIN = 122;


	public static final int CLEAR_COCOS2D_BACKGOUND = 1000;
	//-------------------fragment 对象-----------------------------------------------
	public FragmentManager fragmentManager = null; // fragment 管理

	public LogonViewFragment logonViewFragment = null; // 登录框界面fragment

	public LoginViewFragment loginViewFragment = null; // 登录动画界面fragment

	public CardZoneFragment cardZoneFragment = null; // 分区界面fragment

	public Cocos2dxFragment cocos2dxFragment = null; // 游戏界面fragment

	public LoadingFragment loadingFragment = null; // 分区loading fragment

	public NodifyPasswordFragment nodifyPasswordFragment = null;

	public FreeRoomInfoFragment freeRoomInfoFragment = null; // 自由场房间信息

	public ChallengeFragment challengeFragment = null; // 约战场房间界面

	public AmusementFragment amusementFragment = null;

	protected static SystemPopMsg sysPopMsg = null;
	/**
	 * 请求用户推广码
	 * 
	 */
	public static void getSPKey(final int gameId, final int userID) {

		final HttpConnector http = NetService.getInstance().createHttpConnection(null);
		http.addEvent(new IRequest() {

			@Override
			public void doError(Message msg) {
				NetService.getInstance().removeHttpConnector(http.getTarget());
			}

			@Override
			public void handler(byte[] buf) {
				String spKeyStr = TDataInputStream.getUTF8String(buf);
				PopularizeSPKey popularizeSPKey = new PopularizeSPKey(spKeyStr);
				AppConfig.spKey = popularizeSPKey.getSpKey();
				NetService.getInstance().removeHttpConnector(http.getTarget());
			}

			@Override
			public String getParam() {
				StringBuffer buffer = new StringBuffer();
				String method = "get_spread_key";
				// int uid = HallDataManager.getInstance().getUserMe().userID;
				buffer.append("method=").append(method);
				buffer.append("&gameid=").append(gameId);
				buffer.append("&uid=").append(userID);
				buffer.append("&format=").append("xml");
				buffer.append("&apikey=").append(CenterUrlHelper.apikey);
				buffer.append("&op=").append(System.currentTimeMillis());
				String params = buffer.toString();
				String sign = CenterUrlHelper.getSign(params, CenterUrlHelper.secret);
				return params + sign;
			}

			@Override
			public String getHttpUrl() {
				// return SPREAD_MAIN;
				return AppConfig.WEIXIN_SHARE;
			}

			@Override
			public byte[] getData() {
				return null;
			}
		});
		http.connect();
	}

	
	

	/**
	 * 登录成功后操作，请求切换游戏
	 * 
	 * @param userID
	 * @param gameID
	 */
	protected void querySwitchGame(int userID, int gameID,final Handler sHandler) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.setFront(false);
		tdous.writeInt(userID);
		tdous.writeInt(gameID);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_LOGIN, MSUB_CMD_CHANGE_GAME_V2_REQ, tdous);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);

		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOGIN, MSUB_CMD_CHANGE_GAME_V2_INFO } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				final byte code = tdis.readByte();
				if (code == 0) { // 切换成功，请求分区数据
					// 请求分区数据
					sHandler.obtainMessage(HANDLER_REQUEST_CARD_ZONE_DATA).sendToTarget();
				} else if (code == 1) { // 切换失败
					String ErrorMsg = tdis.readUTFByte();
					sHandler.sendMessage(sHandler.obtainMessage(HANDLER_CHANGE_GAME_ERROR, ErrorMsg));
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	protected static final short LS_TRANSIT_LOGON = 18;

	protected static final short MSUB_LOGON_GIFT_PACK_RESP = 121;

	/**
	 * 获取登录送数据
	 */
	protected void loginGiftPack() {
		
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON, MSUB_LOGON_GIFT_PACK_RESP } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					byte[] bdata = new byte[tdis.available()];
					tdis.read(bdata);
					String dataStr = new String(bdata, "UTF-8");
					int index = dataStr.indexOf("<reggift>");
					if(index > 0){
						AppConfig.giftPack = dataStr.substring(0, index);
						AppConfig.reggiftPack = dataStr.substring(index, dataStr.length());
					}else{
						AppConfig.giftPack = dataStr;
					}
					// 保存登录送
					Log.e("TAG", "登录送数据： " + dataStr);
					if (!AppConfig.giftPack.equals("") && AppConfig.giftPack.trim().length() > 0) {
						// 保存登录送
						saveLoginGiftPackData(AppConfig.giftPack);
					}
					
					if (!AppConfig.reggiftPack.equals("") && AppConfig.reggiftPack.trim().length() > 0) {
						// 保存登录送
						saveRegGiftPackData(AppConfig.reggiftPack);
					}else{
						removeRegGiftPackData();
					}
					AppConfig.reggiftPack = "";
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}

		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}
	
	public static final short MDM_LOTTERY = 19;
	
	public static final short LSUB_CMD_USER_LOGON_REQ  = 9;

	public static final short LSUB_CMD_USER_LOGON_RESP  = 10;
	
	/**
	 * 获取抽奖送数据
	 */
	protected void luckyDrawPack() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOTTERY, LSUB_CMD_USER_LOGON_RESP } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					
					tdis.setFront(false);
					byte code = tdis.readByte();
					if(code == 0){
						short conLogon = tdis.readShort();//连续登录天数
						short LogonLottery =  tdis.readShort();//登录送抽奖次数
						AppConfig.luckyDrawPack = conLogon + "," + LogonLottery;
						// 保存抽奖送
						Log.e("TAG", "抽奖送数据： " + AppConfig.luckyDrawPack);
					}
					
					if (!AppConfig.luckyDrawPack.equals("") && AppConfig.luckyDrawPack.trim().length() > 0) {
						// 保存登录送
						saveLuckyDrawPackData(AppConfig.luckyDrawPack);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}

		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}
	/**
	 * 保存登录送数据
	 * @param text
	 */
	private void saveLoginGiftPackData(String text) {
		try {
			String uid = String.valueOf(HallDataManager.getInstance().getUserMe().userID);
			Util.setIntSharedPreferences(mAct, uid + "_isshowgift", 0);
			Util.setStringSharedPreferences(mAct,uid, text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存抽奖送数据
	 * @param text
	 */
	private void saveLuckyDrawPackData(String text) {
		try {
			String uid=String.valueOf(HallDataManager.getInstance().getUserMe().userID);
			Util.setStringSharedPreferences(mAct,uid + "_luckyDraw", text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存注册送数据
	 * @param text
	 */
	private void saveRegGiftPackData(String text) {
		try {
			String uid=String.valueOf(HallDataManager.getInstance().getUserMe().userID);
			Util.setStringSharedPreferences(mAct,uid + "_regGift", text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void removeRegGiftPackData() {
		try {
			String uid=String.valueOf(HallDataManager.getInstance().getUserMe().userID);
			Util.setStringSharedPreferences(mAct,uid + "_regGift", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 系统弹框消息
	 */
	protected void registerSystemPopMsg(final Handler sHandler) {
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON, MSUB_SYSMSG_POP_MSG } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream stream = netSocketPak.getDataInputStream();
				short sub = netSocketPak.getSub_gr();
				switch (sub) {
				case MSUB_SYSMSG_POP_MSG:
					if (sysPopMsg == null) {
						sysPopMsg = new SystemPopMsg(stream, mAct);
					} else {
						sysPopMsg.readMsg(stream);
					}
					if (sysPopMsg.totalCount != 0 && sysPopMsg.totalCount == sysPopMsg.getMsgSize()) {
						sHandler.sendEmptyMessage(MSUB_SYSMSG_POP_MSG);
					}
					break;
				}
				return true;
			}
		};
		if(sysPopMsg != null){
			sysPopMsg.newRound();
		}
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	protected void registerLoginGiftSwitch(){
		// 定义接受数据的协议
				short[][] parseProtocol = { { LS_TRANSIT_LOGON, MSUB_SYSMSG_POP_MODE_MSG } };
				// 创建协议解析器
				NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
					@Override
					public boolean doReceive(NetSocketPak netSocketPak) {
						TDataInputStream stream = netSocketPak.getDataInputStream();
						short sub = netSocketPak.getSub_gr();
						switch (sub) {
						case MSUB_SYSMSG_POP_MODE_MSG:
							AppConfig.LoginGiftSwitch = stream.readByte();
							AppConfig.isReceiveLoginGiftSwitch = true;
							break;
						}
						return true;
					}
				};
				if(sysPopMsg != null){
					sysPopMsg.newRound();
				}
				// 注册协议解析器到网络数据分发器中
				NetSocketManager.getInstance().addPrivateListener(nPListener);
				nPListener.setOnlyRun(false);
	}

	/**
	 * 显示系统对话框
	 */
	protected void showSystemPopDialog(Activity mAct) {
		if (sysPopMsg != null) {
			sysPopMsg.showLoginDialog();
			sysPopMsg.showIntervalTimeDialog();
			sysPopMsg.showOneTimeDialog();
		}
	}
	
	/**
	 * 停止弹强弹框
	 */
	public void stopSystemPopDialog(){
		if(sysPopMsg != null){
			sysPopMsg.onDestroy();
			sysPopMsg = null;
		}
	}
	
	/**
	 * 显示退出强弹框
	 * @return 是否显示退出强弹框
	 */
	public boolean showExitSystemPopDialog(){
		if(sysPopMsg != null){
			return sysPopMsg.showExitDialog();
		}
		return false;
	}
	
	/**
	 * @Title: getandsetUserInfo
	 * @Description: 获得并设置用户信息
	 * @version: 2013-2-18 下午03:08:16
	 */
	protected void setUserInfo() {
		// 获得用户信息，并转化为本地UserInfo对象
		MUserInfo muserInfo = Community.getSelftUserInfo();
		UserInfo user = new UserInfo();
		UserInfo.parseUserInfo(muserInfo, user);
		user.Token = LoginInfoManager.getInstance().getToken();
		user.loginType = LoginInfoManager.getInstance().getLoginType();
		HallDataManager.getInstance().setUserMe(user);
	}
	
	
	private static final short MSUB_PUSH_TAGS_REQ = 707;
	private static final short MSUB_PUSH_TAGS_RESP = 708;
	/**
	 * 请求push标签，返回标签 后同步标签
	 * @param handler
	 */
	public void reqPushTags(final Handler handler,final int msgWhat){
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.setFront(false);
		tdos.writeInt(FiexedViewHelper.getInstance().getUserId());
		NetSocketPak nsp = new NetSocketPak(LS_TRANSIT_LOGON, MSUB_PUSH_TAGS_REQ,tdos);
		short[][] parseProtocol = {{LS_TRANSIT_LOGON,MSUB_PUSH_TAGS_RESP}};
		NetPrivateListener npl = new NetPrivateListener(parseProtocol) {
			
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				tdis.readInt();
				int xmlLen = tdis.readShort();
				byte[] buf = new byte[xmlLen];
				try {
					int a = tdis.read(buf);
					String xmlStr = new String(buf);
					Message msg = new Message();
					msg.what = msgWhat;
					msg.obj = xmlStr;
					handler.sendMessage(msg);
				} catch (IOException e) {
				}
				
				return true;
			}
		};
		npl.setOnlyRun(false);
		NetSocketManager.getInstance().addPrivateListener(npl);
		NetSocketManager.getInstance().sendData(nsp);
		nsp.free();
	}



	public CardZoneFragment getCardZoneFragment() {
		return cardZoneFragment;
	}
	
	
}
