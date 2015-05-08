package com.mykj.andr.model;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;

/**
 * 合成
 */
public class MixtureInfo extends AXmlData{
	/** 合成消耗 */
	private Consume mConsume = null;

	/** 索引（index） */
	public int index = 0;
	/** 合成类型（0=乐豆合成，1=道具合成） */
	public byte type = 0;
	/** 要合成乐豆数量 */
	public int bean = 0;
	/** 合成所需乐豆 */
	public int beanCost = 0;
	/** vip等级 */
	public short vip = 0;
	/** vip经验 */
	public int exp = 0;
	/** or 普通玩家成功率（original rate） */
	public byte or = 0;
	/** vip玩家成功率（vip rate） */
	public byte vr = 0;
	/** 道具名称 */
	public String name = "";
	/** 道具描述 */
	public String desc = "";
	/** 道具标识，下载图片用 */
	public String logo = "";
	/** 合成消耗列表 */
	public List<Consume> consumeList;

	public MixtureInfo(TDataInputStream tdis) throws Exception{

		if(tdis==null){
			return;
		}	

		final int len=tdis.readShort();
		MDataMark mark=tdis.markData(len);		
		String dataStr = tdis.readUTF(len);		 
		init(dataStr);
		tdis.unMark(mark);
	}

	@Override
	public void onParseStartTag(XmlPullParser p) {
		String tagName = p.getName();
		if(tagName.equals("c")){
			index = parseInt(p.getAttributeValue("", "ix"));
			type = parseByte(p.getAttributeValue("", "k"));
			bean = parseInt(p.getAttributeValue("", "b"));
			beanCost = parseInt(p.getAttributeValue("", "ct"));
			vip = parseShort(p.getAttributeValue("", "v"));
			exp = parseInt(p.getAttributeValue("", "e"));
			or = parseByte(p.getAttributeValue("", "or"));
			vr = parseByte(p.getAttributeValue("", "vr"));
			name = p.getAttributeValue("", "m");
			desc = p.getAttributeValue("", "d");
			logo = p.getAttributeValue("", "l");
		} else if (tagName.equals("ss")) {		
			consumeList = new ArrayList<Consume>();
		} else if (tagName.equals("s")){
			mConsume = new Consume();
			mConsume.t = parseByte(p.getAttributeValue("", "t"));
			mConsume.id = parseInt(p.getAttributeValue("", "i"));
			mConsume.num = parseInt(p.getAttributeValue("", "n"));
			mConsume.name = p.getAttributeValue("", "m");
			mConsume.logo = p.getAttributeValue("", "l");
			consumeList.add(mConsume);
		}
	}

	@Override
	public void onParseEndTag(XmlPullParser p) {

	}

	@Override
	public void onParseSuccess() {

	}

	/**
	 * 合成消耗
	 */
	public class Consume {
		/**
		 *  t 材料类型 0乐豆，1道具 （type） id
		 * 当t=1时，i表示道具ID，当t=0表示乐豆时，i无意义 num 数量 （number） name 道具名称
		 */
		public byte t = -1;
		public int id = 0;
		public int num = 0;
		public String name = "";
		/** 道具标识，下载图片用 */
		public String logo = "";
	}

	@Override
	public void onParseStartDoc() {
		// TODO Auto-generated method stub
		
	}

}