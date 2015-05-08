package com.mykj.andr.model;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @ClassName: VipXmlData
 * @Description: VIP节点数据
 * @author  
 * @date 2013-6-24 下午04:31:10
 *
 */
public class VipXmlData {
    
	/**VIP类型***/
	public String T;
	/**成长值增加***/
	public String GA;
	/**成长值减少***/
	public String GR;
	/**过期天数***/
	public String OT;
	
	public List<Vip_I_Data> is=new ArrayList<Vip_I_Data>();
	
	public void addVIP_I_Data(Vip_I_Data idata){
		is.add(idata);
	}
	
	public VipXmlData(){
		
	}
	public VipXmlData(String t,String ga,String gr,String ot){
		this.T=t;
		this.GA=ga;
		this.GR=gr;
		this.OT=ot;
	}
  
}
