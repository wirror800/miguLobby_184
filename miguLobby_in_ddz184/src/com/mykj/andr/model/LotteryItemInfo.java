package com.mykj.andr.model;

import java.io.Serializable;

import org.xmlpull.v1.XmlPullParser;

import android.graphics.Bitmap;


/**
 * 抽奖配置属性
 * @author JiangYinZhi
 *
 */
public class LotteryItemInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 索引*/
	public int index;
	/** 类型*/
	public int type;
	/** id*/
	public int id;
	/** 数量*/
	public int num;
	/** 名称*/
	public String name;
	/** 文件路径名称*/
	public String fileName;
	/**item 文件Bitmap*/
	public Bitmap bitmap;


	public LotteryItemInfo(XmlPullParser p){
		index = Integer.parseInt(p.getAttributeValue(null, "i"));
		type = Integer.parseInt(p.getAttributeValue(null, "t"));
		id = Integer.parseInt(p.getAttributeValue(null, "p"));
		num = Integer.parseInt(p.getAttributeValue(null, "c"));
		name = p.getAttributeValue(null, "d");
		fileName = p.getAttributeValue(null, "bit");
	}


}
