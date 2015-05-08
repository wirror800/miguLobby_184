package com.mykj.andr.model;

/**
 * 获奖名单实例
 * 
 * @author JiangYinZhi
 * 
 */
public class LotteryDrowWinner {

	private String name;// 获奖用户名
	private int index;// 道具索引
	private int num;// 奖品个数
	private int proId;// 奖品id
	private String proDes;// 奖品描述

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getProId() {
		return proId;
	}

	public void setProId(int proId) {
		this.proId = proId;
	}

	public String getProDes() {
		return proDes;
	}

	public void setProDes(String proDes) {
		this.proDes = proDes;
	}

}
