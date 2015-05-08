package com.mykj.andr.provider;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.mykj.andr.model.VipXmlData;
import com.mykj.andr.model.Vip_I_Data;

/**
 * 
 * @ClassName: VipXmlParser
 * @Description: 解析并设置XML文件节点
 * @author  
 * @date 2013-6-24 下午05:00:35
 *
 */
public class VipXmlParser {
    
	//下面全部都是XML节点标签
	
	static final String VIP_TAG="vip";

	static final String I_TAG="i";

	static final String PW_TAG="pw";
	
	static final String AW_TAG="aw";

	static final String DES_TAG="des";
	 
	//存贮XML文件数据结构
	public static List<VipXmlData> vips=null;
	
	/**版本号***/
	public static String Ver;
	  
	
	/**
	 * @Title: ParserVipSettingsXml
	 * @Description: 解析请求下发的XML文件
	 * @param content
	 * @return
	 * @version: 2013-6-24 下午05:21:44
	 */
	public static boolean ParserVipSettingsXml(String content) {
		vips=new ArrayList<VipXmlData>();
		boolean isParseSuccess = false;
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(content));
			// 解析事件
			int eventType = p.getEventType();
			
			VipXmlData vipxmlData=null;
			Vip_I_Data vipIData=null;
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if (VIP_TAG.equals(tagName)) {
						//XML中VIP节点数据
						
						String t=p.getAttributeValue(null, "t").trim();
						String ga=p.getAttributeValue(null, "ga").trim();
						String gr=p.getAttributeValue(null, "gr").trim();
						String ot=p.getAttributeValue(null, "ot").trim();
                        
						vipxmlData=new VipXmlData(t,ga,gr,ot);
						
						
					}else if(I_TAG.equals(tagName)){
						//XML中VIP下I节点
						
						String s=p.getAttributeValue(null, "s").trim();
						String g=p.getAttributeValue(null, "g").trim();
						
						vipIData=new Vip_I_Data(s,g);
						
						
						
					}else if(PW_TAG.equals(tagName)){
						String ex=p.getAttributeValue(null, "ex").trim();
						String cm=p.getAttributeValue(null, "cm").trim();
						String lk=p.getAttributeValue(null, "lk").trim();
						String add=p.getAttributeValue(null, "add").trim();
						String fer=p.getAttributeValue(null, "fer").trim();
						
						vipIData.EX=ex;
						vipIData.CM=cm;
						vipIData.LK=lk;
						vipIData.ADD=add;
						vipIData.FER=fer; 
						
					}else if(AW_TAG.equals(tagName)){
						String i=p.getAttributeValue(null, "i").trim();
						String v=p.getAttributeValue(null, "v").trim();
						String des=p.getAttributeValue(null, "des").trim();
						if(vipIData!=null){
							vipIData.AW_I=i;
							vipIData.V=v;
							vipIData.DES=des;
						}
						
					}else if(DES_TAG.equals(tagName)){
						if(vipIData!=null){
							vipIData.DES_I=p.getAttributeValue(null, "i").trim();
						}
					}
					break;
				case XmlPullParser.END_TAG:
					String endtagName = p.getName();
					if (VIP_TAG.equals(endtagName)) {
						vips.add(vipxmlData);
					}else if(I_TAG.equals(endtagName)){
						vipxmlData.addVIP_I_Data(vipIData);
					}
					isParseSuccess = true;
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (XmlPullParserException  pe) {
		}catch (Exception e) {
			e.printStackTrace();
			isParseSuccess = false;
		}
		return isParseSuccess;
	} 
}
