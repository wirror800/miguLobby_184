package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.mykj.andr.model.GoodsItem;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/***
 * 
 * @ClassName: GoodsItemProvider
 * @Description: 商城列表数据
 * @author
 * @date 2012-8-6 上午11:26:43
 * 
 */
public class GoodsItemProvider {
	private static GoodsItemProvider instance;
	private List<GoodsItem> list = null;
	private boolean isFinish = false;

	
	
	private GoodsItemProvider() {
		list = new ArrayList<GoodsItem>();
	}

	public static GoodsItemProvider getInstance() {

		synchronized (GoodsItemProvider.class) {
			if (instance == null)
				instance = new GoodsItemProvider();
		}
		return instance;
	}

	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}

	/**
	 * 判断商城列表是否接收完成
	 * 
	 * @return
	 */
	public boolean getFinish() {
		return isFinish;
	}

	
	public void goodsListClear(){
		list.clear();
	}
	
	
	public void add(GoodsItem info) {
		// list.add(0, info);// list倒序
		list.add(info);// list正序
	}

	public List<GoodsItem> getGoodsList() {
		return list;
	}

	public GoodsItem findGoodsItemById(int id) {
		GoodsItem goodsItem = null;
		for (GoodsItem item : list) {
			if (item.shopID == id) {
				goodsItem = item;
				break;
			}
		}
		return goodsItem;
	}

	/**
	 * 获取小钱包购买状态 
	 * true：list中包含小钱包；
	 *  false：：list中没有小钱包；
	 * 
	 * @return
	 */
	public boolean hasSmallMoneyPkg(Context c) {
		if(needShowSmallPkg(c)){
			GoodsItem good = findGoodsItemById(AppConfig.smallMoneyPkgPropId);
			return good == null ?false:true;
		}
		return false;
	}

	/**
	 * 隐藏掉小钱包
	 * @param bool
	 * @return
	 */
	public void removeSmalMoneyPkg(Context c) {
		if (hasSmallMoneyPkg(c))
			list.remove(findGoodsItemById(AppConfig.smallMoneyPkgPropId));
	}
	/*
	 * 简化操作，屏蔽此接口 public GoodsItem[] getGoodsItems() { return list.toArray(new
	 * GoodsItem[list.size()]); }
	 */
	
	/**
	 * 是否需要显示小钱包
	 * @param c
	 * @return
	 */
	private boolean needShowSmallPkg(Context c){
		return Util.providersNameIsYidong(c);
	}
}
