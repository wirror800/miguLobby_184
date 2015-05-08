package com.mykj.andr.provider;


import org.cocos2dx.util.GameUtilJni;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.GoodNews;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.Util;
/***
 * 
 * @ClassName: ScrollDataProvider
 * @Description: 赚话费专区，约战专区 用户报名参加显示滚动提示
 * @author
 * @date 2012-10-10 下午05:54:37
 * 
 */
public class ScrollDataProvider {

	@SuppressWarnings("unused")
	private Activity mAct;

	private static ScrollDataProvider instance;

	private TextView     tvBroadcast;


	private static final short MDM__REALTIME=1000;
	private static final short MSUB_REALTIME_MSG = 1;



	private static final int HANDLER_GOODNEWS_SUCCESS=1;
	/**
	 * 私有构造函数
	 * @param act
	 */
	private ScrollDataProvider(Activity act) {
		this.mAct = act;
	}

	/**
	 * 单例构造
	 * @param act
	 * @return
	 */
	public static ScrollDataProvider getInstance(Activity act) {
		synchronized (ScrollDataProvider.class) {
			if (instance == null)
				instance = new ScrollDataProvider(act);
		}
		return instance;
	} 



	@SuppressLint("HandlerLeak")
	private Handler GoodNewsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_GOODNEWS_SUCCESS:
				GoodNews news=(GoodNews) msg.obj;
				sendToGoodNews(news.getMsg());
				break;
			default:
				break;
			}
		}

	};


	//---------------------------public 方法--------------------------------------------------------------------------

	public void initialize(View v){
		tvBroadcast = (TextView) v;
		goodNewsListener();
	}





	//-------------------------------协议监听------------------------------------------------------

	/**
	 * 喜报协议监听	
	 */
	private void goodNewsListener(){
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM__REALTIME, MSUB_REALTIME_MSG } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				GoodNews news = new GoodNews(tdis);
				Message msg=GoodNewsHandler.obtainMessage();
				msg.obj=news;
				msg.what=HANDLER_GOODNEWS_SUCCESS;
				GoodNewsHandler.sendMessage(msg);
				//sendToGoodNews(news.getMsg());
				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);

	}


	//-------------------------private 方法-----------------------------------------------
	/***
	 * @Title: setScrollTextView
	 * @Description: 设置控件内文本
	 * @param item
	 * @version: 2012-10-11 上午10:09:32
	 */
	private void setScrollTextView(String item){
		if(Util.isEmptyStr(item)){
			tvBroadcast.setVisibility(View.INVISIBLE);
		}else{
			tvBroadcast.setVisibility(View.VISIBLE);
			tvBroadcast.setText(item);
		}
	}




	/**
	 * 按界面分发喜报消息
	 * @param content
	 */
	private void sendToGoodNews(String content){
		int indexSrc=FiexedViewHelper.getInstance().getCurFragment();
		if (indexSrc == FiexedViewHelper.COCOS2DX_VIEW) {
			GameUtilJni.onGoodNews(content);   //发送给游戏
		}else if(indexSrc==FiexedViewHelper.CARDZONE_VIEW){
			setScrollTextView(content);                //发送给分区
		}
	}





}
