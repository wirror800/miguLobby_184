package com.mykj.andr.model;

import java.io.Serializable;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;

public class ActionInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	public String name;	 //活动名称
	public String iconID=""; // 0=热，1=火
	public String state;  //活动状态 (1=正在进行2=即将开始0=已关闭)
	public String date_begin;   //活动开始时间
	public String date_end;   //活动结束时间
	public String logoName; //活动logo 的名称
	public String picName; //活动图片名称
	public String details="";   //活动简介
	public String url; // 活动跳转链接id
	String dataStr="";
	public String type; // 显示区域：1推荐活动，0 非推荐活动
	public String bitmapUrl; // 活动logo
	public String baseUrl; // 活动logo地址
	public int uo;  // 活动链接的打开方式 1 直接内嵌打开 2 浏览器打开

	public ActionInfo(){}

	public ActionInfo(TDataInputStream tdis, String baseUrl){
		if(tdis==null){
			return;
		}	
		//short dataLen=tdis.readShort(); // 此数据块长度
		//tdis.markLen(dataLen);

		final int len=tdis.readShort();
		MDataMark mark=tdis.markData(len);

		//dataStr = tdis.readUTFData(dataLen);
		dataStr =tdis.readUTF(len);
		parseActionInfoAttribute(dataStr);
		this.baseUrl = baseUrl;

		//tdis.unMark();
		tdis.unMark(mark);
	}

	/**
	 * xml中解析键值对
	 * @param strXml
	 * @param tagName
	 * @return
	 */
	public void parseActionInfoAttribute(String strXml) {
		try {
			strXml = strXml.replaceAll("&", "&amp;");
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(strXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (p.getName().equals("act")) {
						name = p.getAttributeValue(null, "ti");
						iconID = p.getAttributeValue(null, "id");
						state = p.getAttributeValue(null, "st");
						date_begin = p.getAttributeValue(null, "be");
						date_end = p.getAttributeValue(null, "en");
						logoName = p.getAttributeValue(null, "lo");
						picName = p.getAttributeValue(null, "bi");
						details = p.getAttributeValue(null, "br");
						url = p.getAttributeValue(null, "de");
						type = p.getAttributeValue(null, "ty");
						bitmapUrl = p.getAttributeValue(null, "bm");
						String str= p.getAttributeValue(null, "uo");
						try{
							uo=Integer.parseInt(str);
						}catch(NumberFormatException e){
							uo=1;
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
