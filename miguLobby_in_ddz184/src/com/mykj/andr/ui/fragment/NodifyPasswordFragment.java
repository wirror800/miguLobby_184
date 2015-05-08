package com.mykj.andr.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.cocos2dx.util.GameUtilJni;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.mingyou.accountInfo.AccountItem;
import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.community.Community;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.CustomActivity;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.UtilHelper;


//临时账号转注册账号请求
public class NodifyPasswordFragment extends FragmentModel implements View.OnClickListener, View.OnTouchListener {
	public final static String TAG = "NodifyPasswordFragment";

	static final short MDM_LOGIN = 12;

	static final short MSUB_CMD_TAT_TO_AT = 15;

	// 失败：返回
	static final short SUB_CMD_LOGIN_V2_ERR = 6;

	// 返回数据格式与上相同 错误编码103--注册账号绑定失败
	// 成功：返回
	static final short MSUB_CMD_LOGIN_V2_USERINFO = 7;

	/** 修改账号密码成功 **/
	public static final int HANDLER_ACCOUNT_SUCCESS = 12157;

	/** 修改账号密码失败 **/
	public static final int HANDLER_ACCOUNT_FAIL = 12156;

	/** 倒计时结束 **/
	public static final int HANDLER_TIMER_END = 12158;

	/** 密码 */
	private String mNewPassword = null;

	/** 用户Token */
	public static final String USER_TOKEN = "user_token";

	public String userToken = "";


	private Activity act;

	private Resources mResource;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.act = activity;
		this.mResource = act.getResources();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.nodify_account_password_fragment, container, false);
		initialize(view);
		return view;
	}

	@Override
	public int getFragmentTag() {
		return FiexedViewHelper.NODIFY_ACCOUNT_VIEW;
	};

	
	
	@Override
	public void onBackPressed() {
		
		((CustomActivity)act).cmccExitGame();
		
		GameUtilJni.exitApplication();
	};

	Button ivConfirm;

	Button ivCancel;

	EditText edt_account;

	EditText edt_password;

	EditText edt_password2;

	EditText edt_share;

	// 进度条框
	ProgressDialog proDialog = null;

	private void initialize(View view) {
		edt_account = (EditText) view.findViewById(R.id.edt_account);
		//UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		//edt_account.setText(userInfo.account);

		edt_password = (EditText) view.findViewById(R.id.edt_password);
		edt_password2 = (EditText) view.findViewById(R.id.edt_password2);
		edt_share = (EditText) view.findViewById(R.id.etShare);

		ivConfirm = (Button) view.findViewById(R.id.ivConfirm);
		ivConfirm.setOnClickListener(this);
		ivCancel = (Button) view.findViewById(R.id.ivCancel);
		ivCancel.setOnClickListener(this);
	}

	/**
	 * @Title: sendMSUB_CMD_TAT_TO_AT
	 * @Description: 发送临时账号转注册账号请求
	 * @author Link
	 * @version: 2011-8-25 上午09:28:31
	 * @param spKey 
	 * @param account2 
	 */
	public void sendMSUB_CMD_TAT_TO_AT(final String account, final String password, boolean isCanWriete, String spKey) {
		receiveLoginListener();
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(Community.gamePlatform);
		tdos.writeUTFByte(HallDataManager.getInstance().getUserMe().Token);
		tdos.writeUTFByte(password);
		//		if (isCanWriete) {
		tdos.writeUTFByte(account);
		//		}
		tdos.writeUTFByte(spKey);

		NetSocketPak sockData = new NetSocketPak(MDM_LOGIN, MSUB_CMD_TAT_TO_AT, tdos);
		// 发送协议
		NetSocketManager.getInstance().sendData(sockData);
	}

	public void receiveLoginListener() {
		short parseProtocol[][] = new short[][] { { MDM_LOGIN, SUB_CMD_LOGIN_V2_ERR }, { MDM_LOGIN, MSUB_CMD_LOGIN_V2_USERINFO } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				if (netSocketPak.getSub_gr() == MSUB_CMD_LOGIN_V2_USERINFO) { // 修改账号密码成功
					TDataInputStream dis = netSocketPak.getDataInputStream();
					dis.setFront(false);
					/** 添加玩家信息完整性检查 11-11-02 */
					UserInfo userInfo = new UserInfo();
					/** 用户ID */
					userInfo.userID = dis.readInt();
					/** 用户头像索引 */
					userInfo.setFaceId((short) dis.readInt());
					/** 用户性别 */
					userInfo.gender = dis.readByte();
					/** 用户会员等级 */
					userInfo.memberOrder = (byte) dis.readInt();
					/** 用户经验 */
					userInfo.experience = dis.readInt();
					/** 用户密码 */
					userInfo.password = dis.readUTFByte();
					/** 用户帐号 */
					userInfo.account = dis.readUTFByte();
					/** 用户昵称 */
					userInfo.nickName = dis.readUTFByte();
					/**
					 * StatusBit 状态位定义（32个bit中） 第 1 bit: 0-表示不能购买道具
					 * 1-可以购买道具（PC为0x00000001） 第 2 bit: 0-表示MTK购买走社区流程
					 * 1-可以MTK购买走MTK流程（PC为0x00000002）
					 */
					final int statusBit = dis.readInt();
					/** 省市编码 */
					dis.readUTF(4);
					/** 用户token串 */
					final String Token = dis.readUTFByte();
					userInfo.Token = Token;
					/** 登录类型 */
					final byte loginType = dis.readByte();
					userInfo.loginType = loginType;
					/** 用户乐豆 */
					userInfo.bean = dis.readInt();
					userInfo.masterRight = dis.readInt();// 用户管理权限
					userInfo.muid = dis.readInt();// 移动社区ID(MUID)
					userInfo.guid = dis.readLong();//用户Guid
					int dou = 0;
					String info = null;
					Message message = handler.obtainMessage(HANDLER_ACCOUNT_SUCCESS, null);

					try{
						dou = dis.readInt(); // 赠送乐豆数量
						info = dis.readUTFByte(); // 推广奖励信息

						List<String> rewardInfo = new ArrayList<String>();
						rewardInfo.add(String.valueOf(dou));
						rewardInfo.add(String.valueOf(info));

						message.obj = rewardInfo;
						Log.e("TAG", "dou:"+dou);
						Log.e("TAG", "info:"+info);
					}catch(Exception e){
						Log.e("TAG", "info:失败");
					}

					HallDataManager.getInstance().setUserMe(userInfo);

					Log.e("修改账号密码成功", "修改账号密码成功");
					// 后台绑定流程是这样的。
					// 1.修改密码成功后，退出游戏，同时发起后台绑定
					// 2.普通账号登陆成功后判断muid是否为0，如果是0，发起后台绑定并退出，否则直接退出（退出时判断）

					// 奖励信息，第一个为赠送乐豆数量，第二个为推广奖励信息
					handler.sendMessage(message);

				} else if (netSocketPak.getSub_gr() == SUB_CMD_LOGIN_V2_ERR) { // 修改账号密码失败
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					final byte err_code = tdis.readByte();
					String msg = mResource.getString(R.string.info_net_busy);
					if (err_code != 0) {
						msg = tdis.readUTFByte();
					}
					// 发送handler消息
					Message hmsg = handler.obtainMessage(HANDLER_ACCOUNT_FAIL, msg);
					handler.sendMessage(hmsg);
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_ACCOUNT_SUCCESS:

				if (proDialog != null) {
					proDialog.dismiss();
					proDialog = null;
				}

				// LoginInfoManager.getInstance().deleteCurAccountInfo();
				// //删除旧账号记录，避免又自动生成临时账号
				// LoginInfoManager.getInstance().setIsBind(true);
				String douCount = null; //赠送乐豆数量
				String info = null; // 推广奖励信息
				if(msg.obj != null){
					List<String> obj = (List<String>)msg.obj;
					List<String> rewardInfo = obj;
					douCount = rewardInfo.get(0);
					info = rewardInfo.get(1);
				}

				final UserInfo userInfo = HallDataManager.getInstance().getUserMe();
				AccountItem accountItem = new AccountItem(userInfo.account, mNewPassword, userInfo.Token, AccountItem.ACC_TYPE_COMM, userInfo.userID);
				LoginInfoManager.getInstance().updateAccInfo(accountItem);
				//final boolean isBindAcc = LoginInfoManager.getInstance().isHasAccontCMCC();

				UtilHelper.showAccountNodifySuccess(act, accountItem.getUsername(), userInfo.nickName, douCount, info, new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 退出程序
						onBackPressed();
					}
				});
				break;

			case HANDLER_ACCOUNT_FAIL: // 接受错误数据
				if (proDialog != null) {
					proDialog.dismiss();
					proDialog = null;
				}
				// Toast.makeText(act, (String)(msg.obj),
				// Toast.LENGTH_SHORT).show();
				UtilHelper.showCustomDialog(act, (String) (msg.obj));

				break;
			case HANDLER_TIMER_END:
				if (proDialog != null) {
					proDialog.dismiss();
					proDialog = null;
				}
				break;
			}
		}
	};

	class Timer implements Runnable {
		Handler handler;

		public Timer(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {

			try {
				Thread.sleep(15000);
				handler.sendEmptyMessage(HANDLER_TIMER_END);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ivConfirm) {
			String account = edt_account.getText().toString();
			mNewPassword = edt_password.getText().toString();
			String oldPassword = edt_password2.getText().toString();
			String spKey = edt_share.getText().toString();
			if (account.trim().length() < 6) {
				Toast.makeText(act, mResource.getString(R.string.info_account_error), Toast.LENGTH_SHORT).show();
			} else if (mNewPassword.trim().length() < 6) {
				Toast.makeText(act, mResource.getString(R.string.info_password_error), Toast.LENGTH_SHORT).show();
			} else if (!mNewPassword.equals(oldPassword)) {
				Toast.makeText(act, mResource.getString(R.string.info_repassword_error), Toast.LENGTH_SHORT).show();
			} else if(spKey != null && spKey.length()>0 && (!checkSpKey(spKey))){
				Toast.makeText(act, mResource.getString(R.string.info_tuiguangma_error), Toast.LENGTH_SHORT).show();
			} else {
				boolean isCanWrite = false;
				UserInfo userInfo = HallDataManager.getInstance().getUserMe();
				if (!account.equals(userInfo.account)) {
					isCanWrite = true;
				}
				// 新增进度框屏蔽
				proDialog = ProgressDialog.show(act, "", mResource.getString(R.string.info_connecting), true, false);
				sendMSUB_CMD_TAT_TO_AT(account, mNewPassword, isCanWrite, spKey);

				new Thread(new Timer(handler)).start();
			}
		} else if (id == R.id.ivCancel) {
			// 退出程序
			onBackPressed();
			//GameUtilJni.exitApplication();
		}
	}

	private boolean checkSpKey(String spKey){
		String gameId = Integer.toHexString(AppConfig.gameId);
		int userId = HallDataManager.getInstance().getUserMe().userID;
		String userIdStr = Integer.toHexString(userId);
		String spTmp = gameId + userIdStr;
		if (spKey.indexOf(gameId) == 0 && (spKey.compareToIgnoreCase(spTmp)!=0)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
}
