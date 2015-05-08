package com.MyGame.Midlet.service;

public class HelpInfo {
	private String phoneNum;
	private String qqNum;  
	private String email;
	private String qqGroup;
	
	
	private static HelpInfo instance=null;
	private HelpInfo(){
		
	}
	
	
	public static HelpInfo getInstance(){
		if(instance==null){
			instance=new HelpInfo();
		}
		return instance;
	}
	
	
	public void  init(String phoneNum,String qqNum,String email,String qqGroup){
		this.phoneNum=phoneNum;
		this.qqNum=qqNum;
		this.email=email;
		this.qqGroup=qqGroup;
	}


	public String getPhoneNum() {
		return phoneNum;
	}


	public String getQQNum() {
		return qqNum;
	}


	public String getEmail() {
		return email;
	}


	public String getQQGroup() {
		return qqGroup;
	}
	
	
	
}
