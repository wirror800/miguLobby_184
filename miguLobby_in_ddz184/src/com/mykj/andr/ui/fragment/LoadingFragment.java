package com.mykj.andr.ui.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.login.utils.UtilDrawableStateList;
import com.login.view.LoginView;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.task.GameTask;
import com.mykj.andr.ui.widget.LoginAssociatedWidget;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/*****
 * 
 * @ClassName: LoadingWidget
 * @Description:加载控件
 * @author zhanghuadong
 * @date 2012-12-15 下午12:39:44
 * 
 */
public class LoadingFragment extends FragmentModel{
	public static final String TAG="LoadingFragment";

	private LoginView mView;

	private Activity mAct=null;
	//private Resources mResource;

	private  NodeDataType mType=NodeDataType.NODE_TYPE_NOT_DO;
	
	private String mLoading;


	public enum NodeDataType {
		NODE_TYPE_101, NODE_TYPE_109, NODE_TYPE_111, NODE_TYPE_NOT_DO;
	}



	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mAct=activity;
		//mResource = mAct.getResources();
	}

	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mView != null)
			handle.sendEmptyMessage(0);
	}



	@Override
	public int getFragmentTag() {
		return FiexedViewHelper.LOADING_VIEW;
	}
	
	@Override
	public void onBackPressed() {
		cancelLoading();
	}
	


	/**
	 * 设置loading 内容
	 * @param content
	 */	
	public void setLoadingType(String loading,NodeDataType type){
		mLoading=loading;
		mDotCount = 0;
		if(type != null){
			this.mType=type;
		}
		if(mView!=null){
			mView.setLoadingText(loading);
		}

	}

	private int mDotCount = 0;
	private void setText(int count){
		String additional;
		switch(count){
		case 0:
			additional = "";
			break;
		case 1:
			additional = ".";
			break;
		case 2:
			additional = "..";
			break;
		case 3:
			additional = "...";
			break;
			default:
				additional = "";
				break;
		}
		
		if(mView != null){
			mView.setLoadingText(mLoading + additional);
		}
	}
	
	
	@SuppressLint("HandlerLeak")
	private Handler handle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			setText(mDotCount);
			mDotCount = (mDotCount + 1) % 4;
			handle.sendEmptyMessageDelayed(0, 1000);
		}
	};
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView=getLoginView(mAct);
		if(!Util.isEmptyStr(mLoading)){
			mView.setLoadingText(mLoading);
		}
		startLoadingAnimation();
		return mView;
	
	}

	private LoginView getLoginView(Activity act){
		LoginView v=new LoginView(act);
		v.setBackgroundRes(R.drawable.new_bg);

		if(AppConfig.all_in_lobby){
			v.setImgZYLogo(R.drawable.zhiyuntoplogo);
			v.setImgProgressRes(R.drawable.loading01);
			v.setImgButtomZYLogo(R.drawable.zhiyunbottomlogo);
			
			v.setImgLogo(R.drawable.common_chinamobilechess);
		}else{
			v.setImgLogo(R.drawable.common_logo);   
		}

		v.setScrollTextBackgroundRes(R.drawable.common_notice_panel);
	
	
		
		v.setLoadingText(mAct.getResources().getString(R.string.ddz_into_game));
		v.setBtnCancelBackground(UtilDrawableStateList.newSelector(act,
				R.drawable.btn_orange_normal,
				R.drawable.btn_orange_pressed));
		v.setBtnCancelOnclick(new CancleOnclick());
		v.setVersion("V" + Util.getVersionName(mAct));
		return v;

	}
	
	
	private void startLoadingAnimation(){
		if(AppConfig.all_in_lobby){
			Drawable frame1 = mAct.getResources().getDrawable(R.drawable.loading01);
			Drawable frame2 = mAct.getResources().getDrawable(R.drawable.loading02);
			Drawable frame3 = mAct.getResources().getDrawable(R.drawable.loading03);
			Drawable frame4 = mAct.getResources().getDrawable(R.drawable.loading04);
			Drawable frame5 = mAct.getResources().getDrawable(R.drawable.loading05);
			Drawable[] frames={frame1,frame2,frame3,frame4,frame5};
			mView.startLoadingAnimation(frames);
		}else{
			mView.startLoadingAnimation(null);
		}

	}
	

	private class CancleOnclick implements OnClickListener{

		@Override
		public void onClick(View v) {
			cancelLoading();
			
		}
		
	}


	public void cancelLoading(){
		if(FiexedViewHelper.getInstance().getCurFragment()==FiexedViewHelper.LOADING_VIEW){

			switch (mType) {
			case NODE_TYPE_101: // 普通节点(自由、约占)
				// 自由战区/智运会
				RoomData room = HallDataManager.getInstance().getCurrentRoomData(); // 获取速配成功进入房间保存的房间信息
				if (room != null) {
					// 请求离开房间
					GameTask.getInstance().clrearTask();
					LoginAssociatedWidget.getInstance().exitRoom(room.RoomID);
				}
				break;
			case NODE_TYPE_109: // 报名节点
				// 发送退出登录 发送201----106
				if(FiexedViewHelper.getInstance().amusementFragment!=null){
					FiexedViewHelper.getInstance().amusementFragment.exitLogin();
				}
				break;
			case NODE_TYPE_111: // 约战节点
				if(FiexedViewHelper.getInstance().challengeFragment!=null){
					FiexedViewHelper.getInstance().challengeFragment.leaveChallenge();
				}
				break;
			case NODE_TYPE_NOT_DO: // 不进行操作
				break;
			default:
				break;
			}

			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);  //由外面调用
		}

	}



	@Override
	public void onDestroy() {
		super.onDestroy();
		mView.stopAnimtionScroll();
		mView.stopAnimationLogo();
	}


}
