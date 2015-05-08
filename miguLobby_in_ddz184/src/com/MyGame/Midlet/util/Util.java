package com.MyGame.Midlet.util;

public class Util {
	/**
	 * 判断字符串是否为空 true is null
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmptyStr(String str) {
		if (str == null || str.trim().length() == 0) {
			return true;
		} else {
			return false;
		}
	}

}
