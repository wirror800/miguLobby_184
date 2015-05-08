package com.mykj.andr.ui.widget.Interface;

/***
 * 
 * @ClassName: LoginAssociatedInterface
 * @Description: 登录关联组建接口
 * @author zhanghuadong
 * @date 2012-11-9 上午11:37:26
 */
public interface LoginAssociatedInterface {

	static final String TAG="LoginAssociatedInterface";
	/** 4.4.1. 登录操作相关 主协议 */
	static final short MDM_LOGIN = 12;
	
	/** 4.7.1.房间主协议 **/
	static final short MDM_ROOM = 14;
	
	/** 4.7.1.2. 请求离开房间 **/
	static final short MSUB_CMD_LEAVE_ROOM_REQ = 3;
	
	/** 4.4.1.6. 移动账号白名单绑定请求 子协议 */
	static final short MSUB_CMD_TAT_WHITE_BIND = 14;
	/** 登录相关协议返回：失败 */
	static final short MSUB_CMD_LOGIN_V2_ERR = 6;
	/** 登录相关协议返回：成功 */
	static final short MSUB_CMD_LOGIN_V2_USERINFO = 7; 
	
	
	/** 4.6.3.1. 断线信息请求 (子协议) **/
	static final short MSUB_CMD_SELECT_CUT_REQ_EXT = 14;
	/** 断线信息返回 (子协议) **/
	static final short MSUB_CMD_SELECT_CUT_DATA = 9;

	
	
	
	//----------------------------------下面是Handler消息标记------------------------------------------------------
	
	
	/**Handler消息:断线重连有数据返回**/
	 static final int HANDLER_CUT_LINK_HAVE_DATA=91;
	/**Handler消息:没有数据**/
	//static final int HANDLER_CUT_LINK_NOT=90;
	//---------------------------------------------------------------------------------------------------------
	/***
	 * @Title: requestWhiteBind
	 * @Description: 请求绑定白名单
	 * @param platId
	 * @param uid
	 * @param apiKey
	 * @param TAT
	 * @version: 2012-11-9 下午12:29:16
	 */
	public void requestWhiteBind(int platId, int uid, String apiKey,
			String TAT);
	
	
	/**
	 * @Title: queryChangeGame
	 * @Description: 请求切换游戏
	 * @param userID  用户ID
	 * @param gameID  游戏ID
	 * @version: 2012-9-18 下午05:22:02
	 */
	public void querySwitchGame(int userID, int gameID);
	
	/***
	 * @Title: breakLine
	 * @Description: 请求断线信息
	 * @param LobbyType  大厅版本类型： 0 – 免费游戏版（默认） 1 – G+包版
	 * @version: 2012-11-13 上午09:44:44
	 */
	public void breakLine(byte LobbyType);
	
	/***
	 * @Title: exitRoom
	 * @Description: 请求离开房间
	 * @param RoomID
	 * @version: 2012-11-15 上午11:32:45
	 */
	public void exitRoom(int RoomID);
}
