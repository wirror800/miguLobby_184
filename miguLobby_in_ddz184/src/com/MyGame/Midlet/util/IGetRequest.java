package com.MyGame.Midlet.util;

/**
 * Htpp连接地址，参数，返回数据管理接口
 * 
 */
public abstract class IGetRequest {

	/**
	 * 传回的数据处理
	 * 
	 * @param statusCode
	 *            HttpStatus.SC_OK...
	 * @param buf
	 */

	public abstract void httpReqResult(final String buf);

	/**
	 * 所有错误处理，包括网络错误，解析错误
	 * 
	 * @param msg
	 */
	public void doError(String msg) {

	}

}
