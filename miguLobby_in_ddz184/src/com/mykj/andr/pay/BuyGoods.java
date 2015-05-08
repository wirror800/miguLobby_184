package com.mykj.andr.pay;

import com.mykj.andr.model.HPropData;

public class BuyGoods {
	public int userId;// 用户ID
	public int propId; // 道具ID,商品ID
	public String goodsInfo; // 具体描述信息
	public long cliSec;// cliSec客户端标识
	public int propCount; //道具的数量
	public HPropData propData[]; // 道具数据数组
	public String hilightWords = null;   //高亮关键字，以|分隔   2014-2-24，1.7.0增加
}
