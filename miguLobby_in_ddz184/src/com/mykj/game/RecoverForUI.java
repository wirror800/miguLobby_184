package com.mykj.game;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.mingyou.login.RecoverForDisconnect;
import com.mingyou.login.SocketLoginListener;
import com.mykj.comm.log.MLog;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.UtilHelper;

/**
 * 提供给UI层调用断线重连功能
 * @author Administrator
 *
 */
public class RecoverForUI {
	private static String TAG = "RecoverForUI";

	/**防止连续多次的断线重连请求，必须等待上一次请求流程执行结束后*/
	private boolean isReContinueGameing = false;
	
	/**
	 * 断线重连UI接口
	 * @param ctx
	 * @param bool
	 * @param reLinkHandler
	 * @param roomId
	 */
	public void reCutLoginAgain(final Context ctx, final boolean bool,final Handler reLinkHandler, final int roomId) {
		if(isReContinueGameing){
			MLog.e("UI 层 收到重连开始reCutLoginAgain--Error-已存在正在执行的断线重连流程");
			return;
		}
		isReContinueGameing = true;
		MLog.d(TAG, "UI 层 收到重连开始reCutLoginAgain");
		final SocketLoginListener listener = new SocketLoginListener() {

			public void onSuccessed(Message msg) {
				MLog.d(TAG, "快速断线重回成功 :"+isReContinueGameing);
				isReContinueGameing = false;
			}

			public void onFiled(Message msg,int arg) {
				MLog.d(TAG, "reCutLoginAgain onFiled msg = "+msg);
				MLog.d(TAG, "快速断线重回失败 :"+isReContinueGameing);
				isReContinueGameing = false;
				RecoverForDisconnect.getInstance().isSendBeginReconnect = false;
				reLinkHandler.post(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						OnClickListener listener = new OnClickListener() {
							public void onClick(View v) {
								//goToReLoginView();
							}
						};
						UtilHelper.showCustomDialog(ctx, ctx.getResources().getString(R.string.ddz_web_reconnetion_failed), listener);
						com.mykj.game.utils.Log.e(TAG, "快速断线重回失败");
					}
				});
			}
		};
		Log.d(TAG, "UI 层 重连流程 调用 LoginSocket.reContinueGame");
		
		//-----开启断线重连-----
		RecoverForDisconnect.getInstance().start(ctx, listener, bool,roomId != 0 ? roomId : 0);
	}
}
