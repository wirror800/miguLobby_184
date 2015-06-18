package com.mykj.game.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.fragment.Cocos2dxFragment;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;

public class ModiyNickNameDialog extends Dialog implements View.OnClickListener{


	/**请求修改昵称*/
	private static final short SUB_GF_MODIFY_NICK_REQ = 1008; 
	/** 修改昵称返回*/
	private static final short SUB_GF_MODIFY_NICK_RESP = 1009;



	@SuppressWarnings("unused")
	private Context mContext =null;

	private EditText etNickName;

	private TextView tvBean;
   
	private Handler mHandler;

	private int mGold;

	public ModiyNickNameDialog(Context context,Handler handler,int gold) {
		super(context, R.style.dialog);
		mContext = context;
		mHandler=handler;
		mGold=gold;
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_nickname_dialog);
		initDialog();
	}


	public void initDialog(){
		etNickName=(EditText)findViewById(R.id.etNickName);


		tvBean=(TextView)findViewById(R.id.tvBean);
		findViewById(R.id.ivCancel).setOnClickListener(this); // 退出
		findViewById(R.id.btnConfirm).setOnClickListener(this); // 确认	

		String text=mContext.getString(R.string.bean_need)+mGold
				+mContext.getString(R.string.lucky_ledou_2);
		tvBean.setText(text);
		
		setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				//统计所有的关闭本对话框的数据
				AnalyticsUtils.onClickEvent(mContext, UC.EC_278);
			}
		});
	}



	private void reqModifyNickName(final String nickName) {
		// 创建发送的数据包
//		TDataOutputStream tdos = new TDataOutputStream();
//		tdos.writeUTFByte(nickName);
//		NetSocketPak pointBalance = new NetSocketPak(Cocos2dxFragment.MDM_VIDEO_GAMEFRAME,
//				SUB_GF_MODIFY_NICK_REQ, tdos);
//		// 定义接受数据的协议
//		short[][] parseProtocol = { { Cocos2dxFragment.MDM_VIDEO_GAMEFRAME,SUB_GF_MODIFY_NICK_RESP } };
//		// 创建协议解析器
//		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
//			public boolean doReceive(NetSocketPak netSocketPak) {
//				// 解析接受到的网络数据
//				TDataInputStream tdis = netSocketPak.getDataInputStream();
//				tdis.setFront(false);
//				byte result=tdis.readByte();
//				byte len=tdis.readByte();
//				String content=tdis.readUTF(len);
//				Message msg=mHandler.obtainMessage();
//				Bundle b=new Bundle();
//				b.putString("nickname", nickName);
//				b.putString("content", content);
//				msg.setData(b);
//				if(result==0){
//					msg.what=Cocos2dxFragment.HANDLER_MODIY_NICKNAME_SUCCESS;
//				}else{
//					msg.what=Cocos2dxFragment.HANDLER_MODIY_NICKNAME_FAIL;
//				}
//				
//				mHandler.sendMessage(msg);
//				return true;
//			}
//		};
//
//		nPListener.setOnlyRun(false);
//		// 注册协议解析器到网络数据分发器中
//		NetSocketManager.getInstance().addPrivateListener(nPListener);
//		// 发送协议
//		NetSocketManager.getInstance().sendData(pointBalance);
//		// 清理协议对象
//		pointBalance.free();
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.ivCancel) {
			dismiss();
			
		}else if(id == R.id.btnConfirm){
			String nickName = etNickName.getText().toString().trim();
			if(!Util.isEmptyStr(nickName)){
				reqModifyNickName(nickName);
			}
			
			AnalyticsUtils.onClickEvent(mContext, UC.EC_277);
			dismiss();
		}
	}


}