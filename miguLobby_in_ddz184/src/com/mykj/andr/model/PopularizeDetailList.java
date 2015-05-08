package com.mykj.andr.model;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.mykj.game.utils.Log;

public class PopularizeDetailList extends AXmlData {

	public class PopularizeDetail {	
		public int condId; // 条件id
		public String cond; // 达成所需要条件
		public String reward; // 推广奖励
		public String awardTime; // 领取日期
		public byte status;// 奖励是否领取
	}

	private ArrayList<PopularizeDetail> detailList = null;
	private PopularizeDetail detail = null;
	private String data;
	private String element;
	private String condId;
	private String cond;
	private String reward;
	private String awardTime;
	private String status;
	private String statusNote;
	private String xmlStatus;
	private String xmlStatusNote;

	public PopularizeDetailList(String xml) {
		data = "data";
		element = "element";
		condId = "condid";
		cond = "cond";
		reward = "reward";
		awardTime = "award_time";
		status = "status";
		statusNote = "statusnote";
		try {
			init(xml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onParseStartDoc() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onParseStartTag(XmlPullParser p) {
		// TODO Auto-generated method stub
		try {
			String tagName = p.getName();
			if (status.equals(tagName)) {
				if (null != detail) {
					detail.status = parseByte(p.nextText());
				} else {
					xmlStatus = p.nextText();
				}
			}

			if (xmlStatus != null && xmlStatus.equals("0")) {
				if (data.equals(tagName)) {
					detailList = new ArrayList<PopularizeDetail>();
				} else if (element.equals(tagName)) {
					detail = new PopularizeDetail();
				} else if (condId.equals(tagName)) {
					detail.condId = parseInt(p.nextText());
				} else if (cond.equals(tagName)) {
					detail.cond = p.nextText();
				} else if (reward.equals(tagName)) {
					detail.reward = p.nextText();
				} else if (awardTime.equals(tagName)) {
					detail.awardTime = p.nextText();
				}
			} else {
				if (statusNote.equals(tagName)) {
					xmlStatusNote = p.nextText();
					Log.e("XmlDetailList", xmlStatus + "#" + xmlStatusNote);
				}
			}

		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onParseEndTag(XmlPullParser p) {
		// TODO Auto-generated method stub
		if (element.equals(p.getName()) && null != detail && null != detailList) {
			detailList.add(detail);
			detail = null;
		}
	}

	@Override
	public void onParseSuccess() {
		// TODO Auto-generated method stub
		xmlStatus = null;
		xmlStatusNote = null;
	}

	public ArrayList<PopularizeDetail> getDetailList() {
		return detailList;
	}

}
