package com.mykj.andr.model;

import java.util.ArrayList;
import java.util.List;

public class Province {
	private String id;
	private String name;
	private List<City> citys = new ArrayList<City>();
	
	public Province() {
		
	}
	
	public Province(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<City> getCitys() {
		return citys;
	}

	public void setCitys(List<City> citys) {
		this.citys = citys;
	}
	
	public void addCity(City city){
		this.citys.add(city);
	}

	@Override
	public String toString() {
		return this.name;
	}
}
