package com.mykj.andr.ui;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mykj.andr.model.ServerItemInfo;
import com.mykj.andr.provider.ServerCenterProvider;
import com.mykj.andr.ui.adapter.ServerCenterAdapter;
import com.mykj.andr.ui.adapter.ServerCenterAdapter.ViewHolder;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.FileAsyncTaskDownload;
import com.mykj.game.utils.FileAsyncTaskDownload.DownLoadingListener;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

/****
 * 服务中心
 * 
 */
public class ServerCenterActivity extends Activity implements
AdapterView.OnItemClickListener, OnClickListener {

	private static final String TAG = "ServerCenterActivity";

	/** 服务中心文件名 */
	public static final String FILE_NAEM = "sc_config.xml";

	private ServerCenterAdapter mAdapter;

	private LinearLayout progressBackPack;

	private GridView mGridView;

	private Context mContext;


	/**-----------------------------handler what--------------------------------------------*/
	/** 下载失败标记 */
	public static final int DOWNLOADFAIL = 0;
	/** 下载服务中心xml成 */
	public static final int XMLDOWNLOADSUCCESS = 1;
	/** 读取xml文件完成标记 */
	public static final int XMLREADEDSUCCESS = 11;
	/** 下载图片成功后更新UI */
	public static final int REFLASH_GRIDVIEW = 2;
	/** 下载图片失败 */
	public static final int DOWNLOAD_GRIDVIEW_FAIL = 22;

	private DownLoadingListener mDownLoadingListener=new DownLoadingListener(){

		@Override
		public void onProgress(int rate, String strRate) {
		}

		@Override
		public void downloadFail(String err) {

		}

		@Override
		public void downloadSuccess(String path) {
			runServerCenterXml(path);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_center_activity);
		mContext=this;

		((TextView)findViewById(R.id.tvTitle)).setText(R.string.servercenter);
		findViewById(R.id.tvBack).setOnClickListener(this);  //返回

		progressBackPack=(LinearLayout) findViewById(R.id.progressBackPack);

		mGridView = (GridView) findViewById(R.id.gvServer);
		mGridView.setOnItemClickListener(this);

		// 获得网络端服务中心配置文件
		String url = getServerCenterConfigXmlUrl();   //客服中心配置下载url

		String serverCenterDir = getServerCenterDir();//客服中心配置下载保存目录	
		String filename=FILE_NAEM;   //客服中心配置下载保存文件名


		File xmlfile = new File(serverCenterDir,filename);
		if (xmlfile.exists()) { 
			runServerCenterXml(xmlfile.getPath());
		} else {
			new FileAsyncTaskDownload(mDownLoadingListener,filename).execute(url,serverCenterDir,null);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AnalyticsUtils.onPageStart(this);
		AnalyticsUtils.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AnalyticsUtils.onPageEnd(this);
		AnalyticsUtils.onPause(this);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		ViewHolder holder = ((ViewHolder) view.getTag());

		ServerItemInfo obj = ((ServerItemInfo) holder.tvServerItem.getTag());
		if (obj == null)
			return;
		else {
			switch (obj.type) {
			case 0:
				// 打开网页
				UtilHelper.onWeb(ServerCenterActivity.this, obj.target_url);
				break;
			case 1:
			case 2: // 点数专区
				break;
			case 3:
			case 4: // 打开版本信息
				Intent in = new Intent(mContext, VersionInfoActivity.class);
				startActivity(in);
				break;
			case 5: // 打开意见反馈
				Intent intent = new Intent(mContext,FeedbackInfoActivity.class);
				startActivity(intent);
				break;
			case 6:// 乐币充值界面
				break;

			}
		}
	}





	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.tvBack)
			finish();
	}




	/**
	 * 读取服务中心xml文件
	 * 
	 * @param xmlPath
	 */
	private void runServerCenterXml(String xmlPath) {
		new ServerCenterTask().execute(xmlPath);
	}



	/**
	 * 获取服务中心文件夹目录
	 * 
	 * @return
	 */
	private  String getServerCenterDir(){
		return Util.getSdcardPath() + "/.mingyouGames/servercenter/" + AppConfig.gameId;
	}





	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case XMLDOWNLOADSUCCESS: // xml下载成功
				// 读取xml并显示GridView界面
				String xmlPath = msg.obj.toString();
				if (!Util.isEmptyStr(xmlPath)) {
					runServerCenterXml(xmlPath);

				} else {
					Log.e(TAG, "传递服务中心xml文件路径出错！");
				}
				Log.e(TAG, "服务中心xml配置文件下载成功！");
				break;
			case DOWNLOADFAIL: // xml下载失败
				Log.e(TAG, "服务中心xml配置文件下载失败，错误码为：" + msg.arg1);
				Log.e(TAG, "下载服务中心xml文件有误，请检查网络或者url地址！");
				break;
			case REFLASH_GRIDVIEW: // 下载图片完成后更新gridView
				if (mAdapter != null)
					mAdapter.notifyDataSetChanged();// 刷新UI
				break;

			case DOWNLOAD_GRIDVIEW_FAIL:
				Log.e(TAG, "服务中心图片文件下载失败，错误码为：" + msg.arg1);
				break;

			}
		}
	};



	/**
	 * 获取帮助中心url 参数
	 * @return
	 */
	private  String getServerCenterConfigXmlUrl(){

		String token=FiexedViewHelper.getInstance().getUserToken();
		int userId=FiexedViewHelper.getInstance().getUserId();

		int simType=UtilHelper.getMobileCardType(mContext);  //手机卡类型   0： 未知类型卡,1： 移动卡  ,2： 联通卡 , 3： 电信卡

		StringBuffer sb = new StringBuffer();

		sb.append(AppConfig.SERVER_PATH);
		sb.append("?at=");

		try {
			token = URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			token="";
		}
		sb.append(token); // m_config.m_AT
		sb.append("&cid=");

		String CID=getCID();
		sb.append(CID);   //for 兼容

		//sb.append(AppConfig.CID);  

		sb.append("&gameid=");
		sb.append(AppConfig.gameId);
		sb.append("&uid=");
		sb.append(userId);

		sb.append("&cardtype=");
		sb.append(simType);
		return sb.toString();
	}




	/**
	 * 获取老版本CID
	 * @return
	 */
	private String getCID() {
		String channel=AppConfig.channelId;
		if(channel.length() > 3){
			channel=channel.substring(1, channel.length());
		}

		String _childChannel = AppConfig.childChannelId; //子渠道，调整为3位
		if(_childChannel.length() > 3){
			_childChannel = _childChannel.substring(_childChannel.length()-3);
		}else if(_childChannel.length() == 2){
			_childChannel = "0"+_childChannel;
		}else if(_childChannel.length() == 1){
			_childChannel = "00"+_childChannel;
		}

		return channel + "02ANDROID1" + _childChannel;
	}

	/***
	 * 解析服务中心xml文件
	 * @param msgXmlFile xml文件
	 * @return
	 */
	private boolean ParseServerCenterXml(String msgXmlFile) {

		ServerCenterProvider provider=ServerCenterProvider.getInstance();
		provider.init();

		boolean isParseSuccess = false;
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(msgXmlFile));

			// 解析事件
			int eventType = p.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if (tagName.equals("item")) {

						//系统消息
						String title=p.getAttributeValue(null, "title").trim();
						String img_name=p.getAttributeValue(null, "img_name").trim();
						int type =0;
						try {
							type = Integer.parseInt(p.getAttributeValue(
									null, "type").trim());
						} catch (Exception e) {
							type=0;
							Log.e(TAG, "服务中心type属性类型转化错误！");
						}
						String img_url=p.getAttributeValue(null, "img_url").trim();
						String target_url=p.getAttributeValue(null, "target_url").trim();


						ServerItemInfo info=new ServerItemInfo(title, img_name, type, img_url, target_url);
						//添加进提供器中
						provider.addServerItemInfo(info);
						isParseSuccess = true;
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (XmlPullParserException  pe) {
			Log.e(TAG, pe.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
			Log.e(TAG, "解析XML文件出错！XML文件格式有误,或者文件保存格式有误！");
			isParseSuccess = false;
		}
		return isParseSuccess;
	}






	/***
	 * 开启一个异步任务区读取服务中心文件
	 * 
	 * @author zhanghuadong
	 */
	public class ServerCenterTask extends AsyncTask<String, Void, String> {
		String path;
		@Override
		protected String doInBackground(String... params) {
			path=params[0].toString();
			String content=Util.readFromFile(path);

			return content;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (!Util.isEmptyStr(result)) {
				boolean bool=ParseServerCenterXml(result);
				if(bool){
					List<ServerItemInfo> array = ServerCenterProvider.getInstance()
							.getServerList();
					mAdapter = new ServerCenterAdapter(ServerCenterActivity.this,array);

					mGridView.setAdapter(mAdapter);
					progressBackPack.setVisibility(View.GONE);
					mGridView.setVisibility(View.VISIBLE);
				} else {
					if(!Util.isEmptyStr(path)){
						File errFile = new File(path+".err");
						File oldFile = new File(path);
						if(oldFile.exists()){
							errFile.deleteOnExit();
							oldFile.renameTo(errFile);
						}
					}
					Toast.makeText(mContext, mContext.getString(R.string.servercenter_error), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}


	}
	//开启一个异步任务区读取服务中心文件


	
	
}
