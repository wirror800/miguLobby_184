package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import com.mykj.andr.model.MixtureInfo;

public class MixInfoProvider {
	static MixInfoProvider instance;

	List<MixtureInfo> list = null;

	private MixInfoProvider() {
		list = new ArrayList<MixtureInfo>();
	}

	public static MixInfoProvider getInstance() {
		if (instance == null)
			instance = new MixInfoProvider();
		return instance;
	}

	public void init() {
		list.clear();
	}

	/**
	 * @return the list
	 */
	public List<MixtureInfo> getList() {
		return list;
	}

	/**
	 * @param list
	 *            the list to set
	 */
	public void setList(List<MixtureInfo> list) {
		this.list = list;
	}

	public void setMixtureInfoProvider(MixtureInfo[] array) {
		init(); // 初始化数据(清空)
		if (array == null)
			return;
		ArrayList<MixtureInfo> arrayList = new ArrayList<MixtureInfo>(
				array.length);
		for (MixtureInfo t : array) {
			arrayList.add(t);
		}
		setList(arrayList);
	}

	public void addMixtureInfo(MixtureInfo info) {
		list.add(info);
	}

	public MixtureInfo[] getMixtureInfo() {
		return list.toArray(new MixtureInfo[list.size()]);
	}

	public List<MixtureInfo> getMixtureList() {
		return list;
	}
}
