package com.mykj.andr.ui.fragment;

import org.cocos2dx.util.GameUtilJni;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.login.utils.UtilDrawableStateList;
import com.login.view.AccountManager;
import com.login.view.LoginViewCallBack;
import com.login.view.LogonView;
import com.mykj.andr.ui.ServerDialog;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/**
 * 登录界面可输入 fragment
 * */
public class LogonViewFragment  extends FragmentModel{
	
	public static final String TAG="LogonViewFragment";
	
    private Activity mAct;
    private LoginViewCallBack mLoginCallBack;
    
    

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mAct=activity;
	}
   
   
    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	View view=getLogonView(mAct);
    	return view;
    }
    
    
    
    private LogonView getLogonView(Activity act){
    	final LogonView v=new LogonView(act);
    	v.setBackgroundRes(R.drawable.new_bg);   //设置主view背景
    	
    	if(AppConfig.all_in_lobby){
    		v.setImgLogo(R.drawable.normal_logo);//设置view logo
        	v.setBackgroudInput(R.drawable.bg_content); //设置登录view 背景
		}else{
	    	v.setImgLogo(R.drawable.common_logo);
	    	v.setBackgroudInput(R.drawable.logon_center_bg); //设置登录view 背景
		}
    	
    	v.setBackgroudLinearInput(R.drawable.ll_account_psw); 
    	v.setBackgroudBtnChoose(R.drawable.accout_select_bg);
    	v.setBackgroundBtnGetPassWord(R.drawable.btn_login_modify_unuse);//忘记密码
        v.setBtnGetPassWordOnClickCallBack(new View.OnClickListener() {
    		@Override
    		public void onClick(View view) {
    				Toast.makeText(mAct, "此功能暂未开发,敬请期待", Toast.LENGTH_SHORT).show();
    		}
    	});
    	Drawable drawable=UtilDrawableStateList.newSelector(act,
    			R.drawable.btn_login_login_normal,
    			R.drawable.btn_login_login_press);
    	v.setBackgroundBtnLogin(drawable);           //登录
    	v.setBtnLoginOnClickCallBack(new View.OnClickListener() {

    		@Override
    		public void onClick(View view) {
    			v.hideInputKeyBoard();
    			final String account = v.getAccoutInput();
    			final String password = v.getPassWordInput();
    			if(Util.isEmptyStr(account)){
    				Toast.makeText(mAct, "账号不能为空", Toast.LENGTH_SHORT).show();
    			}
    			boolean res=AccountManager.getInstance().accountLogin(mAct,account,password , mLoginCallBack);
    			if(res){
    				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOGIN_VIEW);
    			}
    		}
    	});
    	
    	Drawable drawableServer=UtilDrawableStateList.newSelector(act,
    			R.drawable.btn_server_normal,
    			R.drawable.btn_server_press);
    	v.setBackgroundBtnServer(drawableServer); 
    	//v.hideBtnServer();
    	v.setBtnServerOnClickCallBack(new View.OnClickListener() {

    		@Override
    		public void onClick(View view) {
    			ServerDialog.getOnlineServerIntent(mAct);
    		}
    	});
    	
    	v.setAccountListBackGround(R.drawable.accout_select_bg);//popup listview控件背景
    	v.setAccountListDivider(R.drawable.pw_account_divider);//popup listview 分隔线
    	
    	v.setPopupWindwPush(mAct.getResources().getDrawable(R.drawable.popup_push)); //popup 按键弹出指示
    	v.setPopupWindwPull(mAct.getResources().getDrawable(R.drawable.popup_pull)); //popup 按键缩回指示
    	v.setVersion("V" + Util.getVersionName(mAct));
    	return v;
}
    
    
    
	/**
	 * 设置登录回调函数
	 * @param callback
	 */
	public void setLoginCallBack(LoginViewCallBack callback){
		mLoginCallBack=callback;
	}
    
    
   	@Override
   	public int getFragmentTag() {
   		// TODO Auto-generated method stub
   		return FiexedViewHelper.LOGON_VIEW;
   	}
   	
   	@Override
   	public void onBackPressed() {
   		// TODO Auto-generated method stub
   		GameUtilJni.exitApplication();
   	}
}
