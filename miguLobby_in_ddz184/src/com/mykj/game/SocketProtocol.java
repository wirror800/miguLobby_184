package com.mykj.game;


/**
 * 
 * @ClassName: SocketProtocol
 * @Description: Socket协议主子码
 * @author Administrator
 * @date 2013-4-27 上午11:11:30
 *
 */
public interface SocketProtocol {
	/**登陆送乐豆主码**/
	public static final short LS_TRANSIT_LOGON = 18;
    /**登录送乐豆子码**/
	public static final short MSUB_LOGON_GIFT_PACK_RESP = 121;
	
	//////////////////////////////////////////////////////////////////////////
	
	/** 4.7.1.房间主协议 **/
	public static final short MDM_ROOM = 14;
	/** 4.7.1.1.请求进入房间 */
	public static final short MSUB_CMD_ENTER_ROOM_REQ = 2;
	/** 失败：返回 **/
	public static final short MSUB_CMD_ENTER_ROOM_FAILED = 1;
	/** 成功：返回 **/
	public static final short MSUB_CMD_ENTER_ROOM_SUCCEEDED = 0;
}
