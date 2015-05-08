package com.mykj.andr.model;

import java.io.Serializable;

/**
 * 
 * @ClassName: Vip_I_Data
 * @Description:  Vip中I节点下面全部数据
 * @author  
 * @date 2013-6-24 下午04:25:28
 *
 */
public class Vip_I_Data implements Serializable {
	/**
	 * @Fields serialVersionUID: TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;

	/**等级描述***/
	public  String S;
	/**所需成长值***/
	public String G;
	
	/////////////////////////////////////////
	
	/**经验加成***/
	public String EX;
	/**合成成功率加成***/
	public String CM;
	/**是否允许看牌指数***/
	public String LK;
	/**是否允许看加注指数***/
	public String ADD;
	/**是否允许看凶猛指数***/
	public String FER;
	
	///////////////////////////////////////////
	/**道具ID***/
	public String AW_I;
	/**奖励数目***/
	public String V;
	/**奖励描述***/
	public String DES;
	
	/////////////////////////////////////////////
	
	/**VIP描述***/
	public String DES_I; 
	
	public Vip_I_Data(){
		
	}
	public Vip_I_Data(String s,String g){
		this.S=s;
		this.G=g;
	}
}
