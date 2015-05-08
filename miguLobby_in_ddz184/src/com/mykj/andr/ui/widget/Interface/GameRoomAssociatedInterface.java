package com.mykj.andr.ui.widget.Interface;

import com.mykj.andr.model.NodeData;

/***
 * 
 * @ClassName: GameRoomAssociatedInterface
 * @Description: 游戏房间关联接口
 * @author zhanghuadong
 * @date 2012-11-9 上午11:37:26
 */
public interface GameRoomAssociatedInterface {

	// //////////////////////////游戏房间配置信息///////////////////////////////////////////////
	public static final short MDM_CONFIG = 3; // (主协议号)

	public static final short MSUB_GR_ROOM_INFO = 100; // 游戏房间基本配置信息(下行)

	public static final short MSUB_GR_ROOM_INFO_EX = 105; // 游戏房间扩展配置信息(下行)

	/** 4.7.1.房间主协议 **/
	public static final short MDM_ROOM = 14;

	/** 4.7.1.2. 请求离开房间 **/
	public static final short MSUB_CMD_LEAVE_ROOM_REQ = 3;

	/** 4.7.1.1.请求进入房间 */
	public static final short MSUB_CMD_ENTER_ROOM_REQ = 2;

	/** 失败：返回 **/
	public static final short MSUB_CMD_ENTER_ROOM_FAILED = 1;

	/** 成功：返回 **/
	public static final short MSUB_CMD_ENTER_ROOM_SUCCEEDED = 0;

	/** 快速断线重回：请求 **/
	public static final short MSUB_CMD_CUT_REUTRN_ROOM_REQ = 15;

	/** 快速断线重回结果：返回 **/
	public static final short MSUB_CMD_CUT_RESUTNR_ROOM_RESP = 16;

	/** 主协议 **/
	public static final short MDM_QUICK_PLAY = 16;

	/** 子协议 **/
	public static final short MSUB_QUICK_PLAY_ROOMS_REQ_EXT = 4;

	/** 快速游戏数据返回(子协议) **/
	public static final short MSUB_QUICK_PLAY_ROOMS_DATA = 2;

	/********** 4.8.1.用户操作 主协议 **********/
	public static final short MDM_USER = 2;

	/** 4.8.1.9. 旁观请求 （手机请求）子协议 **/
	public static final short MSUB_GR_USER_LOOKON_REQ = 2;

	/** 4.8.1.1.坐下请求 （手机请求） 子协议 **/
	public static final short MSUB_GR_USER_SIT_REQ = 1;

	/*** 坐下失败 ***/
	public static final short MSUB_GR_SIT_FAILED = 103;

	/*** 子协议：根据用户坐下状态判断是否坐下成功 **/
	public static final short MSUB_GR_USER_STATUS = 101;

	// ----------------------------Handler------------------------------
	/** 速配返回失败消息到Handler */
	public static final int HANDLER_MSUB_QUICK_PLAY_ROOMS_DATA_FAIL = 20;

	/** 速配返回成功消息到Handler */
	public static final int HANDLER_MSUB_QUICK_PLAY_ROOMS_DATA_SUCCESS = 21;

	/** 坐下失败消息handler **/
	public static final int HANDLER_MSUB_GAME_SIT_DOWN_FAILED = 2103;

	/***
	 * @Title: QuickMatchNode
	 * @Description: 速配某节点（找到房间）
	 * @param nodeID
	 *            节点ID
	 * @version: 2012-11-9 上午11:45:39
	 */
	public void quickMatchNode(NodeData node);

	/***
	 * @Title: QuickGame
	 * @Description: 快速游戏
	 * @version: 2012-11-9 下午02:18:12
	 */
	public void quickGame();

	/***
	 * @Title: ExitRoom
	 * @Description: 离开房间
	 * @param roomID
	 *            房间ID
	 * @version: 2012-11-9 上午11:46:05
	 */
	public void exitRoom(int roomID);

	/***
	 * @Title: enterRoom
	 * @Description: 进入房间
	 * @param roomID
	 *            房间ID
	 * @version: 2012-11-9 上午11:47:25
	 */
	public void enterRoom(int roomID);

	/**
	 * @Title: RequestGameSitDown
	 * @Description: 快速自动坐下请求
	 * @version: 2011-7-12 下午05:54:51
	 */
	public void requestGameSitDown();

	public void receiveUserStatus();

	public void receiveRoomConfigData();

}
