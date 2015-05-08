package com.mykj.andr.ui.widget.Interface;


/****
 * 
 * @ClassName: HallAssociatedInterface
 * @Description: 大厅相关接口
 * @author  
 * @date 2012-11-9 下午12:47:38
 *
 */
public interface HallAssociatedInterface {

	static final short LS_TRANSIT_LOGON = 18;

	/** 子协议-个人中心信息请求 */
	static final short MSUB_USERCENTER_INFO_REQ = 115;


	/**破产赠送请求子协议**/
	static final short MSUB_CMD_PRESENT_ASK	 =  26;
	/**赠送返回子协议**/
	static final short  MSUB_CMD_PRESENT_ACK   =   27;

	/**系统消息101**/
	static final short MSUB_SYSMSG_REQUEST=101;

	/**登录公告返回102**/
	static final short  MSUB_SYSMSG_LOGON_NOTICE  = 102;

	/**系统消息返回***/
	static final short MSUB_SYSMSG_ROLL_MSG  = 103;

	/**系统留言返回(个人消息)104**/
	static final short MSUB_SYSMSG_LEAVE_WORD  = 104;





	//------------------------------------handler------------------------------------------------------------------------
	/**破产赠送handler**/
	static final int HANDLER_BANKRUPTCY=2627;

	//------------------------------------------------------------------------------------------------------------
	/***
	 * @Title: getUserCenterInfo
	 * @Description: 个人中心信息请求
	 * @param userId
	 * @param clentId
	 * @version: 2012-11-9 下午12:49:09
	 */
	public void getUserCenterInfo(int userId, int clentId);


	/***
	 * @Title: QuickGame
	 * @Description: 快速游戏
	 * @version: 2012-11-15 下午04:58:25
	 */
	public void quickGame();

	/***
	 * @Title: givingBankruptcy
	 * @Description: 破产赠送
	 * @version: 2012-11-16 下午04:53:52
	 */
	public void givingBankruptcy();


	/****
	 * @Title: requestSystemMessage
	 * @Description: 请求系统消息（消息盒子）
	 * @param userid
	 * @param mobileCode 渠道号+客户端标识
	 * @param noticeVer 默认为"0"
	 * @version: 2012-11-27 下午09:02:37
	 */
	public void requestSystemMessage(int userid);

}
