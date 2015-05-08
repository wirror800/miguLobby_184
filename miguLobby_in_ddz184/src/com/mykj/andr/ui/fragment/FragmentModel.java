package com.mykj.andr.ui.fragment;

import android.support.v4.app.Fragment;

import com.mykj.game.FiexedViewHelper;
import com.mykj.game.ddz.api.AnalyticsUtils;

public abstract class FragmentModel extends Fragment{

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if(!hidden){
			FiexedViewHelper.getInstance().setFragment(getFragmentTag());
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!isHidden()){
			FiexedViewHelper.getInstance().setFragment(getFragmentTag());	
		}
		AnalyticsUtils.onPageStart(getTag());
		
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AnalyticsUtils.onPageEnd(getTag());
	}
	
	abstract public void onBackPressed();
	
	abstract public int getFragmentTag();
}