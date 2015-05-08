package com.mykj.andr.ui;

import java.io.UnsupportedEncodingException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;
import com.mykj.game.utils.WebDialog;

public class ServerDialog extends AlertDialog  implements View.OnClickListener{
	
	public static  String SERVER_PHONE="400-777-9996";
	
	private Context mContext;
	private static  String LIVE800_PATH; //http://v2.live800.com/live800/chatClient/chatbox.jsp?companyID=321701&jid=7688598844";

	
	private static final int MYKJ_KEY=123;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_dialog);
		findViewById(R.id.ivCancel).setOnClickListener(this); // 退出
		findViewById(R.id.tvOnlineServer).setOnClickListener(this); // 在线客服服务
		findViewById(R.id.tvCallServer).setOnClickListener(this); // 客服电话
		findViewById(R.id.btnReport).setOnClickListener(this); // 客服服务



	}



	public ServerDialog(Context context) {
		super(context);
		mContext = context;
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ivCancel) {

		}else if(id == R.id.tvOnlineServer){
			getOnlineServerIntent(mContext);
		}else if(id == R.id.tvCallServer){
			String phonenum=SERVER_PHONE;
			if(!Util.isEmptyStr(phonenum)){
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phonenum));  
				mContext.startActivity(intent); 
			}
		}else if(id == R.id.btnReport){
			mContext.startActivity(new Intent(mContext,
					FeedbackInfoActivity.class));
		}
		dismiss();
	}



	public static void  getOnlineServerIntent(Context context){

		int id=FiexedViewHelper.getInstance().getUserId();
		String userName=FiexedViewHelper.getInstance().getUserNickName();
		String memo="";
		String userId="";//memo参数定义这样的格式：uid_123:cid_8301:gameid_100:gname_斗地:model_asdka";
		try {
			StringBuilder sb=new StringBuilder();
			sb.append("uid").append('_').append(id).append(':');
			sb.append("version").append('_').append(Util.getVersionName(context)).append(':');
			sb.append("gameid").append('_').append(AppConfig.gameId).append(':');
			sb.append("gname").append('_').append(context.getString(R.string.app_name)).append(':');
			sb.append("ntyep").append('_').append(Util.getAPNTypeString(context)).append(':');
			sb.append("operator").append('_').append(Util.getOPID(context)).append(':');
			sb.append("model").append('_').append(Util.getMobileModel()).append(':');
			sb.append("cid").append('_').append(AppConfig.channelId).append(':');

			sb.append("scid").append('_').append(AppConfig.childChannelId);

			userId=java.net.URLEncoder.encode(sb.toString(),"UTF-8");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}


		long timestamp=System.currentTimeMillis();

		String hashEncode="";
		try {
			hashEncode=java.net.URLEncoder.encode(userId+userName+memo+timestamp+MYKJ_KEY,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String hashCode=Util.getMD5(hashEncode);

		String infoValue="";
		try {
			StringBuilder sb=new StringBuilder();
			sb.append("userId").append('=').append(userId).append('&');
			sb.append("name").append('=').append(userName).append('&');
			sb.append("memo").append('=').append(memo).append('&');
			sb.append("hashCode").append('=').append(hashCode).append('&');
			sb.append("timestamp").append('=').append(timestamp);

			infoValue=java.net.URLEncoder.encode(sb.toString(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if(!Util.isEmptyStr(LIVE800_PATH)){
			String url=LIVE800_PATH+"&info="+infoValue;
			//Intent intent = new Intent(Intent.ACTION_VIEW);
			//intent.setData(Uri.parse(url));
			//context.startActivity(intent);
			//UtilHelper.onWeb(context, url, true, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			WebDialog webDialog = new WebDialog(context, R.style.BackgroundOnly, true, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			webDialog.setUrl(url);
		}

	}


	
	/**
	 * 获取在线客服url
	 * @param context
	 */
	public static void reqOnlineServerUrl(Context context){
		StringBuilder sb=new StringBuilder();
		sb.append(AppConfig.onlineServer).append('?');
		sb.append("cid=").append(AppConfig.channelId).append('&');
		sb.append("scid=").append(AppConfig.childChannelId).append('&');
		sb.append("gameid=").append(AppConfig.gameId).append('&');
		sb.append("version=").append(Util.getVersionName(context));
		String url=sb.toString();
		String content=Util.getConfigXmlByHttp(url);
		
		LIVE800_PATH=UtilHelper.parseStatusXml(content, "url");
		SERVER_PHONE=UtilHelper.parseStatusXml(content, "tel");
	}

}
