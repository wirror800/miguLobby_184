package com.mykj.andr.model;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.mykj.game.utils.Log;

public class PopularizeFriendList extends AXmlData {

	public class PopularizeFriend {
		public int uid;
		public String nickName;
		public byte sex;
	}

	private ArrayList<PopularizeFriend> friendList = null;
	private PopularizeFriend friend = null;
	private String data;
	private String element;
	private String uid;
	private String nickname;
	private String sex;
	private String status;
	private String statusNote;
	private String xmlStatus;
	private String xmlStatusNote;

	public PopularizeFriendList(String xml) {
		data = "data";
		element = "element";
		uid = "uid";
		nickname = "nickname";
		sex = "sex";
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
				xmlStatus = p.nextText();
			}
			if (xmlStatus != null && xmlStatus.equals("0")) {
				if (data.equals(tagName)) {
					friendList = new ArrayList<PopularizeFriend>();
				} else if (element.equals(tagName)) {
					friend = new PopularizeFriend();
				} else if (uid.equals(tagName)) {
					friend.uid = parseInt(p.nextText());
				} else if (nickname.equals(tagName)) {
					friend.nickName = p.nextText();
				} else if (sex.equals(tagName)) {
					friend.sex = parseByte(p.nextText());
				}
			} else {
				if (statusNote.equals(tagName)) {
					xmlStatusNote = p.nextText();
					Log.e("XmlFriendList", xmlStatus + "#" + xmlStatusNote);
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
		if (element.equals(p.getName()) && null != friend && null != friendList) {
			friendList.add(friend);
		}
	}

	@Override
	public void onParseSuccess() {
		// TODO Auto-generated method stub

	}

	public ArrayList<PopularizeFriend> getFriendList() {
		return friendList;
	}

}
