package com.mykj.game.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.mykj.andr.model.City;
import com.mykj.andr.model.Province;
import com.MyGame.Midlet.R;

public class ProvinceConfig {
	public static List<Province> provinces;

	public static void initProvinces(Context context) {
		InputStream is = null;
		provinces = new ArrayList<Province>();
		Province defaultProvince = new Province("-1", context.getResources()
				.getString(R.string.chose_province));
		defaultProvince.addCity(new City("-1", context.getResources().getString(R.string.chose_city)));
		provinces.add(defaultProvince);
		Province province = new Province();
		try {
			is = context.getAssets().open("province.txt");
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				String provinceStr;
				while ((provinceStr = br.readLine()) != null) {
					String[] provinceInfo = provinceStr.split(",");
					String provinceName = provinceInfo[0]; // 省份名称
					String cityName = provinceInfo[1]; // 城市名称
					String areaCode = provinceInfo[2]; // 地区编码
					String provinceCode = areaCode.substring(0, 2); // 省份编码
					String cityCode = areaCode.substring(2, 4); // 城市编码

					if (province.getId() != null
							&& !provinceCode.equals(province.getId())) {
						provinces.add(province);
						province = new Province();
					}

					if (province.getId() == null) {
						province.setId(provinceCode);
						province.setName(provinceName);
					}
					if (province.getCitys().isEmpty()) {
						province.addCity(new City("-1", context.getResources()
								.getString(R.string.chose_city)));
					}
					City city = new City();
					city.setId(cityCode);
					city.setName(cityName);
					province.addCity(city);
				}
				provinces.add(province);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
