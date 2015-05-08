package com.mykj.andr.model;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public abstract class AXmlData {
	protected void init(String xml) throws Exception {
		// 定义工厂
		XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		// 定义解析器
		XmlPullParser p = f.newPullParser();
		// 获取xml输入数据
		p.setInput(new StringReader(xml));
		// 解析事件
		int eventType = p.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				onParseStartDoc();
				break;
			case XmlPullParser.START_TAG:
				onParseStartTag(p);
				break;
			case XmlPullParser.END_TAG:
				onParseEndTag(p);
				break;
			case XmlPullParser.END_DOCUMENT:
				onParseSuccess();
				break;
			default:
				break;
			}
			// 用next方法处理下一个事件，否则会造成死循环。
			eventType = p.next();
		}
	}

	public abstract void onParseStartDoc();

	public abstract void onParseStartTag(XmlPullParser p);

	public abstract void onParseEndTag(XmlPullParser p);

	public abstract void onParseSuccess();

	/**
	 * 重写parseShort,避免xml字段不存在异常
	 * 
	 * @param string
	 * @return
	 */
	public static short parseShort(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Short.parseShort(string);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 重写parseInt,避免xml字段不存在异常
	 * 
	 * @param str
	 * @return
	 */
	public static int parseInt(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
			return 0;
		}
	}

	public static byte parseByte(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Byte.parseByte(string);
		} catch (Exception e) {
			return 0;
		}
	}

	/*
	 * @Title: getContent
	 * 
	 * @Description: If current event is START_TAG then if next element is TEXT
	 * then element content is returned or if next event is END_TAG then empty
	 * string is returned, otherwise exception is thrown. After calling this
	 * function successfully parser will be positioned on END_TAG.
	 * 
	 * @param p
	 * 
	 * @return String 返回类型
	 * 
	 * @throws
	 * 
	 * @version: 2012-8-15 上午10:38:51
	 */
	public String getContent(XmlPullParser p) {
		String content = null;
		try {
			content = p.nextText();
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		return content;
	}
}
