package com.mykj.andr.ui.widget.Interface;

/****
 * 
 * @ClassName: InvokeViewCallBack
 * @Description: 切换不同View方法
 * @author
 * @date 2012-12-13 下午01:48:41
 *
 */
public interface InvokeViewCallBack {

	void skipToCocods2dView();
	/***
	 * @Title: skipToCardZoneView
	 * @Description: 跳转到分区界面(不同参数不同类型的跳转)
	 * @param type  参数类型
	 * @version: 2012-12-13 下午03:57:35
	 */
	void skipToCardZoneView();
	void skipToLoginView();
	//从大厅登录进来回调(for android)
	/***
	 * @Title: hallComeInSuccess
	 * @Description: 从大厅登录进来回调
	 * @version: 2012-12-13 下午03:57:35
	 **/
	void hallComeInSuccess();
}
