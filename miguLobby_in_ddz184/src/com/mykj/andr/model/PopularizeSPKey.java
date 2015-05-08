package com.mykj.andr.model;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.mykj.game.utils.Log;

public class PopularizeSPKey extends AXmlData {

	private static String spKey;
	private String spKeyTag;
	private String status;
	private String statusNote;
	private String xmlStatus;
	private String xmlStatusNote;

	public PopularizeSPKey(String xml) {
		spKeyTag = "spkey";
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
				if (spKeyTag.equals(tagName)) {
					spKey = p.nextText();
				}
			} else {
				if (statusNote.equals(tagName)) {
					xmlStatusNote = p.nextText();
					Log.e("XmlSPKey", xmlStatus + "#" + xmlStatusNote);
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
	}

	@Override
	public void onParseSuccess() {
		// TODO Auto-generated method stub
		xmlStatus = null;
		xmlStatusNote = null;
	}

	public String getSpKey() {
		return spKey;
	}

}
