package com.mykj.andr.model;

import java.io.Serializable;

import android.content.res.Resources;

import com.mykj.andr.headsys.HeadInfo;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;

/**
 * 
 * @ClassName: GoodsItem
 * @Description: 商城商品
 * @author Administrator
 * @date 2012-7-23 下午03:58:44
 * 
 */
public class GoodsItem implements Serializable {

	//添加是否重复标记(与协议无关)，主要用于客户端刷选
	public boolean isDouble=false;
	/**
	 * 默认折叠，图标向下，同时不显示子布局控件
	 */
	public boolean isArrowUp=false;
	
	public GoodsItem(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		
		//int dataLen = dis.readShort(); // 此数据块长度
		//dis.markLen(dataLen);

		final int len=dis.readShort();
		MDataMark mark=dis.markData(len);
		
		shopID = dis.readInt(); // 商城ID
		shopType = dis.readInt(); // 商城父节点编号（所属子类型）
		pointValue = dis.readInt(); // 商品点数(价格)

		goodsName = dis.readUTFByte(); // 名称数据
		goodsPhoto = dis.readUTFByte(); // 图片文件名
		mCoin = dis.readInt(); // 星乐币价格 2012.6.5
		cornID = dis.readByte();// 角标(图标)ID 2012.7.18

		goodsPresented = dis.readUTFByte(); // 赠送描述信息
		goodsDescrip = dis.readUTFShort(); // 商品描述
		urlId = dis.readShort();
		priceUnit = dis.readShort();
		priceName = dis.readUTFShort();
		hilightWords = dis.readUTFShort();
		//dis.unMark();
		dis.unMark(mark);
		 
	}

	public GoodsItem(byte[] array) {
		this(new TDataInputStream(array));
	}

	/**
	 * @Fields serialVersionUID: TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;
	public int shopID; // 商城ID
	public int shopType; // 商城父节点编号（所属子类型）
	public int pointValue; // 商品点数(价格)   
	public int mCoin; // 星币价格
	public String goodsName; // 名称数据
	public String goodsPhoto=""; // 图片文件名
	public String goodsDescrip=""; // 商品详细描述

	public String goodsPresented; // 赠送
	public byte cornID; // 标记，热卖，打折，推荐
	public short urlId;//道具跳转wap的url
	private short priceUnit;//道具价格单位  0:点,1:分

	public String priceName;       //单位名字
	public String hilightWords;    //高亮字段
	
	
	/**
	 * 如果对象类型是GoodsItem 的话 则返回true 去比较hashCode值
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (obj instanceof GoodsItem) {
			GoodsItem item = (GoodsItem) obj; 
			if (item.shopID == this.shopID)
				return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * 头像信息转换为道具信息，主要用于购买时使用，因此仅需初始化购买时所需信息
	 * @param head
	 */
	public GoodsItem(HeadInfo head){
		shopID = head.getId(); // 商城ID
		pointValue = head.getCost(); // 商品点数(价格)   
		goodsName = head.getName(); // 名称数据
		goodsDescrip=head.getFullDesc(); // 商品详细描述
		goodsPresented = ""; // 赠送
		priceUnit = head.getCurrencyType();//道具价格单位  0:点,1:分
		hilightWords = head.hilightWords;
	}
	
	
	/**
	 * 重写hashcode 方法，返回的hashCode 不一样才认定为不同的对象
	 */

	@Override
	public int hashCode() {
		 return Integer.valueOf(shopID).hashCode(); // 只比较id，id一样就不添加进集合
	}

	
	/**
	 * 获取商城物品的价格，已经做了单位的自动转换
	 * @return
	 */
	public String getGoodsPrice(){
		Resources resources =AppConfig.mContext.getResources();
		String strPrice=resources.getString(R.string.market_price);
		
//		if(priceUnit==0){
//			strPrice+=pointValue+resources.getString(R.string.market_point);
//		}else if(priceUnit==1){
//			int yuan=pointValue/100;
//			int yushu=pointValue-yuan*100;
//			if(yushu==0){
//				strPrice+=yuan+resources.getString(R.string.market_yuan);
//			}else{
//				int jiao=yushu/10;
//				int fen=yushu-jiao*10;
//				if(fen==0){
//					strPrice+=yuan+"."+jiao+resources.getString(R.string.market_yuan);
//				}else{
//					strPrice+=yuan+"."+jiao+fen+resources.getString(R.string.market_yuan);
//				}
//			}
//		}
		strPrice = strPrice + getGoodsValue();
		return  strPrice;
	}
	
	
	/**
	 * 获取商城物品的价格，已经做了单位的自动转换
	 * @return
	 */
	public String getGoodsValue(){
//		Resources resources =AppConfig.mContext.getResources();
		String strPrice="";
		
//		if(priceUnit==0){
//			strPrice=pointValue+resources.getString(R.string.market_point);
//		}else if(priceUnit==1){
//			int yuan=pointValue/100;
//			int yushu=pointValue-yuan*100;
//			if(yushu==0){
//				strPrice=yuan+resources.getString(R.string.market_yuan);
//			}else{
//				int jiao=yushu/10;
//				int fen=yushu-jiao*10;
//				if(fen==0){
//					strPrice=yuan+"."+jiao+resources.getString(R.string.market_yuan);
//				}else{
//					strPrice=yuan+"."+jiao+fen+resources.getString(R.string.market_yuan);
//				}
//			}
//		}
		if(priceUnit==1 || priceUnit==5){
			int yuan=pointValue/100;
			int yushu=pointValue-yuan*100;
			if(yushu==0){
				strPrice=yuan + priceName;
			}else{
				int jiao=yushu/10;
				int fen=yushu-jiao*10;
				if(fen==0){
					strPrice=yuan+"."+jiao+priceName;
				}else{
					strPrice=yuan+"."+jiao+fen+priceName;
				}
			}
		}else{
			strPrice=pointValue+priceName;
		}
		return  strPrice;
	}
	
}
