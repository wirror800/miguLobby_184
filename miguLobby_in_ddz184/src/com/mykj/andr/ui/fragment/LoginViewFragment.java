package com.mykj.andr.ui.fragment;


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
import com.login.view.AccountManager;
import com.login.view.LoginView;
import com.login.view.LoginViewCallBack;
import com.mingyou.login.LoginSocket;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.ChannelDataMgr;
import com.mykj.game.utils.Util;

public class LoginViewFragment extends FragmentModel{
	public static final String TAG="LoginViewFragment";

	private Activity mAct;

	private LoginViewCallBack mLoginCallBack;

	private LoginView mView;




	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mAct=activity;
	}



	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mView != null)
			handle.sendEmptyMessage(0);
	}



	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * The Fragment's UI is just a simple text view showing its
	 * instance number.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView=getLoginView(mAct);

		startLoadingAnimation();  //移动滚筛子动画
		return mView;
	}




	private LoginView getLoginView(Activity act){
		LoginView v=new LoginView(act);
		v.setBackgroundRes(R.drawable.new_bg);

		if(AppConfig.all_in_lobby){
			v.setImgZYLogo(R.drawable.zhiyun_logo);
			v.setImgProgressRes(R.drawable.loading01);
			v.setImgButtomZYLogo(R.drawable.migu_logo);
			
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




	/**
	 * 快速登录接口
	 */
	public void quickLogin(){
		String token =ChannelDataMgr.getInstance().getCmccToken();
	
		if(Util.isEmptyStr(token)){
			AccountManager.getInstance().quickEntrance(mLoginCallBack);
		}else{
			LoginSocket.getInstance().closeNet();
			AccountManager.getInstance().thirdQuickEntrance(mLoginCallBack,token);
		}
		
	
	}



	/**
	 * 设置登录回调函数
	 * @param callback
	 */
	public void setLoginCallBack(LoginViewCallBack callback){
		mLoginCallBack=callback;
	}


	private void startLoadingAnimation(){
		if(AppConfig.all_in_lobby){
			Drawable frame1 = mAct.getResources().getDrawable(R.drawable.loading01);
			Drawable frame2 = mAct.getResources().getDrawable(R.drawable.loading02);
			Drawable frame3 = mAct.getResources().getDrawable(R.drawable.loading03);
			Drawable frame4 = mAct.getResources().getDrawable(R.drawable.loading04);
			Drawable frame5 = mAct.getResources().getDrawable(R.drawable.loading05);
			
			Drawable[] frames = { frame1, frame2, frame3, frame4, frame5};
			mView.startLoadingAnimation(frames);
		}else{
			mView.startLoadingAnimation(null);
		}

	}


	public void setLoginText(String str){
		loginText = str;
		mDotCount = 0;
	}

	private int mDotCount = 0;
	private String loginText = "";
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
			mView.setLoadingText(loginText + additional);
		}
	}
	
	public void setText(String text){
		if(mView != null){
			mView.setLoadingText(text);
		}
	}
	
	private Handler handle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			setText(mDotCount);
			mDotCount = (mDotCount + 1) % 4;
			handle.sendEmptyMessageDelayed(0, 500);
		}
	};
	
	public void setBtnCancelOnclick(OnClickListener listener){
		if(mView!=null){
			mView.setBtnCancelOnclick(listener);
		}
	}

	@Override
	public int getFragmentTag() {
		return FiexedViewHelper.LOGIN_VIEW;
	}
	

	@Override
	public void onBackPressed() {
		loginCancle();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		mView.stopAnimtionScroll();
		mView.stopAnimationLogo();
	}

	
	
	
	private class CancleOnclick implements OnClickListener{

		@Override
		public void onClick(View v) {
			loginCancle();
		}

	}


	private void loginCancle(){
		if(FiexedViewHelper.getInstance().getCurFragment()==FiexedViewHelper.LOGIN_VIEW){
			FiexedViewHelper.getInstance().goToReLoginView();
		}
	}

}
