package com.mykj.andr.model;

import java.io.Serializable;

public class VipPrivilege implements Serializable {

	/**
	 * @Fields serialVersionUID: TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;
	
	public int vipDegree;             //等级
	public String experienceAdd;      //经验加成
	public String conposeRate;        //合成率
	public String weekAward;          //周奖励
	public String otherExponent;      //其他指数
}
