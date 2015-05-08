package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import com.mykj.andr.model.RankOrderInfo;
import com.mykj.andr.ui.RankOrderDialog.DataChangeListener;

public class RankOrderProvider {
    
	private static RankOrderProvider instance;
	
	private List<RankOrderInfo> mList=new ArrayList<RankOrderInfo>();
	private DataChangeListener listener;
	private boolean isFinish=false;
	
	private RankOrderProvider(){
		
	}
	
	public void setFinishStatus(boolean b){
		isFinish=b;
	}
	
	public boolean getFinishStatus(){
		return isFinish;
	}
	
	public static RankOrderProvider getInstance(){
		if(instance==null)
			instance=new RankOrderProvider();
		return instance;
	}
	
	
	public void clear(){
		if(mList.size()>0){
			mList.clear();
			if(listener != null){
				listener.onDataChanged();
			}
		}
	}
	
	public void add(RankOrderInfo info){
		mList.add(info);
		if(listener != null){
			listener.onDataChanged();
		}
	}
	
	
	public void setList(List<RankOrderInfo> list) {
		mList.clear();
		mList.addAll(list);
		if(listener != null){
			listener.onDataChanged();
		}
	}
	
	public List<RankOrderInfo> getList(){
		return mList;
	}

	public void setListener(DataChangeListener listener) {
		// TODO Auto-generated method stub
		this.listener = listener;
	}
}
