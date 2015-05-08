package com.mykj.andr.headsys;

import org.xmlpull.v1.XmlPullParser;


/**
 * @author Administrator wanghj
 * 头像信息，根据商品而来
 */
public class HeadInfo {
	private int id = -1;          //表情id
	private String name = "";     //表情名字
	private String desc = "";     //表情描述
	private String Shortdesc = ""; //简短说明
	private int cost = -1;        //表情价格
	private String unit = "";     //表情价格单位
	private int expireTime = -1;  //过期时间
	private int cornId = 0;      //角标值，1：打折，2：热卖，3，推荐
	private short currencyType = 0;  //购买货币类型 0点数，1钱， 3金币， 4乐豆
	public String hilightWords = null;   //购买时需要高亮的字段，用|分隔
	private boolean isHaved = false;
	public boolean isArrowUp = false;
	/**
	 * 由网络数据初始化头像
	 * @param p
	 */
	public HeadInfo(XmlPullParser p){
		try{
			id = Integer.parseInt(p.getAttributeValue(null, "id"));
		}catch (Exception e) {
			// TODO: handle exception
			id = 0;
		}
		
		name = p.getAttributeValue(null, "n");
		desc = p.getAttributeValue(null, "ds");
		try{
			cost = Integer.parseInt(p.getAttributeValue(null, "p"));
		}catch(Exception e){
			cost = -1;
		}
		unit = p.getAttributeValue(null, "ps");
		Shortdesc = p.getAttributeValue(null, "gd");
		try{
			cornId = Integer.parseInt(p.getAttributeValue(null, "ci"));
		}catch(Exception e){
			cornId = 0;
		}
		try{
			currencyType = Short.parseShort(p.getAttributeValue(null, "pt"));
		}catch(Exception e){
			currencyType = 0;
		}
		if(cost <= 0){
			markHaved();
		}
		
		hilightWords = p.getAttributeValue(null, "hlt");
	}
	
	/**
	 * 由本地new个头像，当某头像在商城列表下架而用户购买后使用时间没到期的时候用到
	 */
	public HeadInfo(int id){
		this.id = id;
		name = "";
		desc = "";
		cost = -1;
		unit = "";
		expireTime = -1;
	}
	
	/**
	 * 获得id
	 * @return
	 */
	public int getId(){
		return id;
	}
	
	/**
	 * 获得商品价格，组装后的
	 * @return
	 */
	public String getGoodsPrice(){
		if(cost < 0){     //
			return "暂不出售";
		}else if(cost == 0){
			return "免费";
		}else if(currencyType == 1){   //分，元转换
			int yuan=cost/100;
			int yushu=cost%100;
			if(yushu==0){
				return yuan+unit;
			}else{
				int jiao=yushu/10;
				int fen=yushu%10;
				if(fen==0){
					return yuan+"."+jiao+unit;
				}else{
					return yuan+"."+yushu+unit;
				}
			}
		}else{
			return cost + unit;
		}
	}
	
	/**
	 * 获得商品价格数
	 * @return
	 */
	public int getCost(){
		return cost;
	}
	
	/**
	 * 是否已经拥有
	 * @return
	 */
	public boolean isHaved(){
		return isHaved;
	}
	
	/**
	 * 标记已获得
	 */
	public void markHaved(){
		isHaved = true;
	}
	
	/**
	 * 获得过期时间
	 */
	public int getExpireDate(){
		return expireTime;
	}
	
	/**
	 * 获得简单描述
	 * @return
	 */
	public String getShortDesc(){
		return Shortdesc;
	}
	
	/**
	 * 获得头像名称
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * 获得完整描述
	 * @return
	 */
	public String getFullDesc(){
		return desc;
	}
	
	/**
	 * 获得角标id
	 * @return
	 */
	public int getCornId(){
		return cornId;
	}
	
	/**
	 * 获得货币类型
	 * @return
	 */
	public short getCurrencyType(){
		return currencyType;
	}
	
	/**
	 * 设置头像名称
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * 设置头像描述
	 * @param desc
	 */
	public void setFullDesc(String desc){
		this.desc = desc;
	}
	
	/**
	 * 设置过期时间
	 * @param time
	 */
	public void setExpireTime(int time){
		expireTime = time;
	}
}
