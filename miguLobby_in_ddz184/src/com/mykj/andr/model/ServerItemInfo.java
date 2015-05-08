package com.mykj.andr.model;

import java.io.Serializable;

import android.graphics.drawable.Drawable;


/********
 * 服务中心实体类
 * @author 
 *
 */
public class ServerItemInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
   public ServerItemInfo(){}
	
	public ServerItemInfo(String title,String img_name,int type,
			String img_url,String target_url){
		
		this.title=title;
		this.img_name=img_name;
		this.type=type;
		this.img_url=img_url;
		this.target_url=target_url;
	}
	
	
	public Drawable mDrawable=null;
	 
    
    
    /**网络配置文件属性等**/
    public String title="";
    public String img_name="";          //图片名称  比如：xxxx.png
    public String target_url="";
    public int type=0;
    public String img_url="";            //网络图片地址 比如：http://www.ssdffiosfsdofosdfisd/xxx.png
}
