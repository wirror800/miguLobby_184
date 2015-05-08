package com.mykj.andr.ui.widget.Interface;


/**
 * 
 * @ClassName: ActionInfoInterface
 * @Description: 活动列表接口
 * @author  
 * @date 2012-11-15 上午11:48:50
 *
 */
public interface ActionInfoInterface {
	 
	static final short LS_TRANSIT_LOGON = 18;
	
	/** 活动相关常量 */
	  static final short MSUB_CMD_ACTIVITY_LIST_ASK = 32;
	  static final short MSUB_CMD_ACTIVITY_LIST_ACK = 33;
	  static final int HANDLER_ACT_QUERY_SUCCESS_NODATA = 330;
	  static final int HANDLER_ACT_QUERY_SUCCESS = 331;
	
	 
	  
	  
	  
	/***
	 * @Title: requestActionInfoList
	 * @Description: 请求获得列表
	 * @version: 2012-11-15 上午11:49:23
	 */
	 public void requestActionInfoList();
}
