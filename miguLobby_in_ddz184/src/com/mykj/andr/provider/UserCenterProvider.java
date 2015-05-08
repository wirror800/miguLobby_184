package com.mykj.andr.provider;

import com.mykj.andr.model.UserCenterData;

public class UserCenterProvider {
	private static UserCenterProvider instance;
	private UserCenterData userCenterData;
	
	private UserCenterProvider(){
		
	}
	
	public static UserCenterProvider getInstance() {
		if (instance == null)
			instance = new UserCenterProvider();
		return instance;
	}

	public UserCenterData getUserCenterData() {
		return userCenterData;
	}

	public void setUserCenterData(UserCenterData userCenterData) {
		this.userCenterData = userCenterData;
	}
	
	
}
