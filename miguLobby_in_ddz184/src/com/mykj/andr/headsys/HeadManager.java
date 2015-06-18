package com.mykj.andr.headsys;

import java.io.File;
import java.io.StringReader;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.pay.PayManager;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;
import com.mykj.comm.io.TDataOutputStream;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;


/**
 * @author Administrator wanghj
 * 头像系统管理
 */
public class HeadManager{
	private static final String TAG = "HeadManager";
	public static final int ZONEHEAD = 1;        //分区头像
	public static final int GAMEHEAD = 2;        //游戏头像
	private static HeadManager instance = null;
	private int curId = 0;               //当前id
	private int askUseId = 0;            //请求使用id
	private static final int HEAD_TYPE = 1;    //与服务器配套
	private static final short LS_MDM_PROP = 17;                 //手机道具主协议
	private static final short LSUB_CMD_EXTEND_SHOP_LIST_REQ=758;  //请求扩展商品列表
	private static final short LSUB_CMD_EXTEND_SHOP_LIST_RESP=759; //接收扩展商品列表
	private static final short LSUB_CMD_EXTEND_PACK_LIST_REQ=808;  //请求扩展背包列表
	private static final short LSUB_CMD_EXTEND_PACK_LIST_RESP=809; //接收扩展背包列表
	private static final short LSUB_CMD_MODIFY_USER_HEAD_REQ=810;  //请求使用头像
	private static final short LSUB_CMD_MODIFY_USER_HEAD_RESP=811;  //接收使用结果
	
	private static final int HANDLER_DOWNLOAD_IMG = 0;       //通知下载图片
	private static final int HANDLER_REQUEST_PACK = 1;       //通知下载头像道具id
	private static final int HANDLER_GETPACK_FINISH = 2;     //获得头像道具完成
	//解析应用列表结果
	public static final int PARSE_NO_NODE = 0;			//没有子节点
	public static final int PARSE_SUCCESS = 1;			//解析成功
	public static final int PARSE_FAIL = 2;				//解析失败
	
	private List<HeadInfo> heads = new ArrayList<HeadInfo>();         //头像列表
	private HashMap<String, SoftReference<Drawable>> iconMap = new HashMap<String, SoftReference<Drawable>>();   //头像图片缓存
	private Handler updateHandler = null;    //更新ui
	private int updateHandlerMsgWhat = 0;    //更新ui消息
	private String url;                      //下载url
	private String fileName = "";            //文件名
	private boolean isDownloading = false;   //是否正在下载
	private final String PNG = ".png";     //png后缀
	private Context mAct = null;
	public static HeadManager getInstance(){
		if(instance == null){
			instance = new HeadManager();
		}
		return instance;
	}
	
	private HeadManager(){
	}
	
	/**
	 * 设置头像上下文，作为弹框用
	 * @param context
	 */
	public void setContext(Context context){
		mAct = context;
	}
	
	/**
	 * 设置更新ui handler
	 * 当数据改变时发消息通知更新
	 * @param handler
	 * @param updateHandlerMsgWhat
	 */
	public void setUpdateHanler(Handler handler, int updateHandlerMsgWhat){
		updateHandler = handler;
		this.updateHandlerMsgWhat = updateHandlerMsgWhat;
	}
	
	/**
	 * 
	 * @param id 道具id
	 * @return 0表示金币或乐豆购买的头像，1表示虚拟币或移动点数， -1表示这不是头像
	 */
	public short getPayType(int id){
		if(heads != null){
			for(HeadInfo head : heads){
				if(head.getId() == id){
					if(head.getCurrencyType() == 3 || head.getCurrencyType() == 4){  //用金币或乐豆买
						return 0;
					}
					else{
						return 1;
					}
				}
			}
		}
		return -1;   //不在列表中
	}
	


	
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case HANDLER_DOWNLOAD_IMG:      //下载图片
				startDownloadImg();
			break;
			case HANDLER_REQUEST_PACK:      //请求头像道具列表
				UserInfo u = HallDataManager.getInstance().getUserMe();
				if(u != null){
					requestHeadPackList(u.userID);
				}
				break;
			case HANDLER_GETPACK_FINISH:    //道具列表请求完毕
				int faceId = HallDataManager.getInstance().getUserMe().getFaceId();   //上次使用id
				for(HeadInfo head : heads){
					if(head.getId() == faceId){ 
						if(head.isHaved()){           //拥有，继续使用
							if(curId == 0){
								curId = faceId;
								if(updateHandler != null){   //通知更新
									updateHandler.obtainMessage(updateHandlerMsgWhat).sendToTarget();
								}
							}
						}else{
							if(curId == askUseId){
								requestUseHead(curId);       //不再拥有，请求使用新的
							}
						}
						break;
					}
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	
	/**
	 * 下载图片
	 */
	private void startDownloadImg(){
		if(isDownloading){   //正在下载中
			return;
		}
		if(!Util.isMediaMounted()){    //没有存储卡
			return;
		}
		//先确定路径创建好
		File dir = new File(HeadConfig.zoneHeadSavePth);
		if(!dir.exists()){
			dir.mkdirs();
		}
		dir = new File(HeadConfig.gameHeadSavePth);
		if(!dir.exists()){
			dir.mkdirs();
		}
		
		for(HeadInfo head : heads){
			String pth = getImgFullFileName(head.getId(), ZONEHEAD);   //先查分区图片
			String downloadFileName = null;
			if(pth!=null){
				pth = getImgFullFileName(head.getId(), GAMEHEAD);   //有分区图像则查找游戏图片
				if(pth == null){
					pth = HeadConfig.gameHeadSavePth + "h_" + head.getId();   //没有图片，则用游戏图片无后缀文件路径
					downloadFileName = head.getId() + "_big" + PNG;
				}
			}else{
				pth = HeadConfig.zoneHeadSavePth + "h_" + head.getId();    //没有图片，用分区图片无后缀文件路径
				downloadFileName = head.getId() + PNG;
			}
			if(downloadFileName == null){   //下载文件名为null，表示有图片，查找下个id
				continue;
			}
			
			//组装下载url地址
			
			url = AppConfig.HEAD_ICON_URL + "/" + downloadFileName;
			
			if(Util.isNetworkConnected(AppConfig.mContext)){
				if(url.endsWith(PNG)){
					fileName = pth + PNG;
				}
				if(!isDownloading){   //当前不在下载
					new Thread(){
						public void run(){
							isDownloading = true;   //标识
							boolean downRlt = Util.downloadResByHttp(url, fileName);   //下载
							if(downRlt){
								handler.sendEmptyMessageDelayed(HANDLER_DOWNLOAD_IMG, 500); //通知下载下一个，延迟防ANR
							}else{
								handler.sendEmptyMessageDelayed(HANDLER_DOWNLOAD_IMG, 2000); //延迟下载
							}
							if(downRlt && updateHandler != null){  //下载成功通知更新ui
								updateHandler.sendEmptyMessage(updateHandlerMsgWhat);
							}
							isDownloading = false;  //标识
						}
					}.start();
				}
				return;
			}

			}
		}
	
	/**
	 * 获取图片完整文件名
	 * @param id
	 * @param type ZONEHEAD， GAMEHEAD
	 * @return
	 */
	private String getImgFullFileName(int id, int type){
		String dir;
		if(type == ZONEHEAD){
			dir = HeadConfig.zoneHeadSavePth;
		}else{
			dir = HeadConfig.gameHeadSavePth;
		}
		String fileName = dir + "h_"+ id;
		File file=new File(fileName + PNG);
		if(file.exists() && !file.isDirectory()){
			Drawable icon = getDrawableFromFile(AppConfig.mContext, file,
					DisplayMetrics.DENSITY_HIGH);     //获得drawable
			if(Util.isDrawableAvailable(icon)){
				if(type == ZONEHEAD){   //是分区这边的，则直接缓存起来，免得二次解析
					iconMap.put("h_" + id, new SoftReference<Drawable>(icon));
				}
				return file.getAbsolutePath();
			}else{
				file.deleteOnExit();   //无效文件则删除
			}
		}
		return null;
	}
	
	
	private int needW = 0;     //分区头像需要的宽，缩放用
	private int needH = 0;     //分区头像需要的高，缩放用
	
	/**
	 * 获得drawable,不用util的因为要进行缩放
	 * @param context
	 * @param pngFile
	 * @param density
	 * @return
	 */
	private Drawable getDrawableFromFile(Context context, File pngFile,
			int density) {
		Bitmap bmp = BitmapFactory.decodeFile(pngFile.getPath());
		if (bmp != null){
			bmp.setDensity(density);
			if(needW == 0){   //未初始化数据，解析默认图片获得宽高
				DisplayMetrics metric = new DisplayMetrics();
		        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
				Drawable ori = context.getResources().getDrawable(R.drawable.ic_female_face);
				needW = ori.getIntrinsicWidth() * density / metric.densityDpi;
				needH = ori.getIntrinsicHeight() * density / metric.densityDpi;
			}
	        Bitmap resizeBmp = Bitmap.createScaledBitmap(bmp, needW, needH, true);   //缩放
	        if(bmp.hashCode() != resizeBmp.hashCode()){    //当要求大小与原大小一致时可能返回本身，所以需要比较hashcode
	        	bmp.recycle();   //将原bitmap释放，否则引起内存溢出
	        }
	        Drawable icon = new BitmapDrawable(context.getResources(), resizeBmp);
			return icon;
		}
		return null;
	}
	
	
	/**
	 * 获取完整文件名，给游戏用，可能返回null，分区返回drawable
	 * @param id
	 * @return
	 */
	public  String getImgFullFileName(int id){
		String pth =  getImgFullFileName(id, GAMEHEAD);
		if(pth == null){    //没图片，下载
			handler.sendEmptyMessage(HANDLER_DOWNLOAD_IMG);
		}
		return pth;
	}
	
	/**
	 * 获得当前自己头像
	 * @param context
	 * @return
	 */
	public Drawable getZoneHead(Context context){
		return getZoneHead(context, curId);
	}
	
	/**
	 * 通过id获得图片，可能返回null
	 * @param context
	 * @param id
	 * @return
	 */
	private Drawable getIconFromFile(Context context, String id){
		Drawable icon = null;
		if(Util.isNetworkConnected(context)){   //需要有储存卡
			try{
				final String fileName = HeadConfig.zoneHeadSavePth + "/"+ id;
				File iconFile = new File(fileName + PNG);
				if(iconFile.exists() && !iconFile.isDirectory()){
					icon = getDrawableFromFile(context, iconFile,
							DisplayMetrics.DENSITY_HIGH);
					if(!Util.isDrawableAvailable(icon)){
						icon = null;
						iconFile.deleteOnExit();
					}
				}
			}catch(Exception e){
				
			}
		}
		return icon;
	}
	
	/**
	 * 通过id获得图片，分区用
	 * @param context
	 * @param id
	 * @return
	 */
	public Drawable getZoneHead(Context context, int id){
		String key = "h_"+id;
		Drawable icon = null;
		if(iconMap.containsKey(key)){  //先在缓存中找
			icon = iconMap.get(key).get();
		}
		if(icon != null){  //缓存中有
			return icon;
		}else{  //缓存中没有
			icon = getIconFromFile(context, key);   //在文件中找
			if(icon != null){  //文件中有，则加入缓存
				SoftReference<Drawable> s = new SoftReference<Drawable>(icon);
				iconMap.put(key, s);
				return icon;
			}
		}
		
		//本地没有，需要下载
		handler.sendEmptyMessage(HANDLER_DOWNLOAD_IMG);	
		
		//返回默认图片
		byte sex = HallDataManager.getInstance().getUserMe().gender;
		if(sex == 0){
			return context.getResources().getDrawable(R.drawable.ic_female_face);
		}else{
			return context.getResources().getDrawable(R.drawable.ic_male_face);
		}
	}
	
	/**
	 * 设置id
	 * @param id
	 */
//	public void setHeadId(int id){
//		curId = id;
//	}
	
	/**
	 * 获得id
	 * @return
	 */
	public int getCurId(){
		return curId;
	}
	
	private int headCount = 0;   //商城中头像总个数
	private boolean isHeadMarketFinish = false;  //头像商城是否下载完毕

	/**
	 * 头像商城是否下载完毕
	 * @return
	 */
	public boolean isGetHeadMarketListFinish(){
		return isHeadMarketFinish;
	}

	private boolean isRequestHeadMarket = false;
	/**
	 * 请求头像商城列表
	 * @param context
	 */
	public void requestHeadMarketList(Context context) {
		if(isRequestHeadMarket){
			return;
		}
		isRequestHeadMarket = true;
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		short type = (short) UtilHelper.getMobileCardType(context); /**1：中国移动,2：中国联通,3：中国电信,4：无卡*/
		if (type == 0) {
			type = 4;
		}
		tdos.writeShort(type);
		tdos.writeShort(HEAD_TYPE);   //1表示头像
		NetSocketPak pointBalance = new NetSocketPak(LS_MDM_PROP,
				LSUB_CMD_EXTEND_SHOP_LIST_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_MDM_PROP, LSUB_CMD_EXTEND_SHOP_LIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				
				int total = tdis.readShort();     //商城头像总数
				int curNum = tdis.readShort();    //当前包所包含头像数
				int type = tdis.readShort(); // 商品类型
				if(type == HEAD_TYPE){   //表示表情类商品
					headCount += curNum;  //已得到的增加
					for(int i = 0; i < curNum; i++){
						try{
							String servData = tdis.readUTFShort();
							parseHeadMarketXml(servData);
						}catch(Exception e){
							Log.i(TAG, "头像商品解析错误："+i);
						}
					}
					if(headCount >= total){  //认为已接收完成
						isHeadMarketFinish = true;
						isRequestHeadMarket = false;
						if(updateHandler != null){
							updateHandler.obtainMessage(updateHandlerMsgWhat).sendToTarget();
						}
						
						handler.obtainMessage(HANDLER_REQUEST_PACK).sendToTarget();    //请求下载道具列表
					}
				}

				return true;
			}
		};
		nPListener.setOnlyRun(false);

		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();
	}
	
	/**
	 * 解析网络数据，商城头像
	 * @param strXml
	 * @return
	 */
	private int parseHeadMarketXml(String strXml) {
		int isParseSuccess = PARSE_NO_NODE;
		if (strXml == null) {
			return isParseSuccess;
		}
		{
			int startIndex = strXml.indexOf("<?");
			if(startIndex > 0){
				strXml = strXml.substring(startIndex);
			}
		}
		
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(strXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if (tagName.equals("h")) {
						HeadInfo head = new HeadInfo(p);
						boolean newData = true;
						for(HeadInfo temp : heads){
							if(temp.getId() == head.getId()){
								newData = false;
								break;
							}
						}
						if(newData){
							heads.add(head);
						}

						isParseSuccess = PARSE_SUCCESS;
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
		} catch (Exception e) {
			Log.v(TAG, "parse xml error");
			isParseSuccess = PARSE_FAIL;
		}
		return isParseSuccess;
	}

	/**
	 * 解析网络数据，背包头像
	 * @param tdis
	 */
	private void parseHeadPackInfo(TDataInputStream tdis){
		int len = tdis.readShort();
		MDataMark mark= tdis.markData(len);
		int id = tdis.readInt();
		int expireDate = tdis.readInt();
		String name = tdis.readUTFShort();
		String desc = tdis.readUTFShort();
		tdis.unMark(mark);
		for(HeadInfo head : heads){   //查找列表，若有数据则更新
			if(head.getId() == id){
				head.setName(name);
				head.setFullDesc(desc);
				head.setExpireTime(expireDate);
				head.markHaved();
				return;
			}
		}
		
		//不存在列表中，表明是个下架又没到期的道具
		HeadInfo head = new HeadInfo(id);
		head.setName(name);
		head.setFullDesc(desc);
		head.setExpireTime(expireDate);
		head.markHaved();
		heads.add(head);
		return;
	}
	
	private boolean isHeadPackFinish = false;    //背包头像是否下载完毕
	private int headPackCount = 0;            //背包头像总数
	private boolean isRequestHeadPack = false;
	/**
	 * 背包头像是否下载完毕
	 * @return
	 */
	public boolean isGetHeadPackFinish(){     
		return isHeadPackFinish;
	}
	
	/**
	 * 请求背包头像列表
	 * @param userID
	 */
	public void requestHeadPackList(int userID) {
		if(isRequestHeadPack){
			return;
		}
		isRequestHeadPack = true;
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userID);
		tdos.writeShort(HEAD_TYPE);   //表情类商品
		NetSocketPak pointBalance = new NetSocketPak(LS_MDM_PROP, LSUB_CMD_EXTEND_PACK_LIST_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_MDM_PROP, LSUB_CMD_EXTEND_PACK_LIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);

					int total = tdis.readShort(); // 商品总个数
					int type = tdis.readShort(); //商品类型
					if(type == HEAD_TYPE){  //头像
						
						int num = tdis.readShort(); // 当次商品个数
						headPackCount += num;
						for(int i = 0; i < num; i++){
							parseHeadPackInfo(tdis);
						}
					}
					
					if(headPackCount >= total){
						isHeadPackFinish = true;
						isRequestHeadPack = false;
						if(updateHandler != null){
							updateHandler.obtainMessage(updateHandlerMsgWhat).sendToTarget();  //下载完通知更新ui
						}
						handler.obtainMessage(HANDLER_GETPACK_FINISH).sendToTarget();
					}
					

				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};

		nPListener.setOnlyRun(false);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();
	}
	
	
	/**
	 * 清空头像商城数据
	 */
	public void clearHeadMarketInfo(){
		heads.clear();
		headCount = 0;
		curId = 0;
		askUseId = 0;
		isHeadMarketFinish = false;
		isRequestHeadMarket = false;

	}
	
	/**
	 * 清空头像背包数据
	 */
	public void clearHeadPackInfo(){
		headPackCount = 0;
		isHeadPackFinish = false;
		isRequestHeadPack = false;
	}
	
	/**
	 * 获得头像列表
	 * @return
	 */
	public List<HeadInfo> getHeadInfoList(){
		return heads;
	}
	
	/**
	 * 购买成功
	 * @param id
	 */
	public void buyHeadSuccess(int id){
		time = 0;    //重设计时器
		for(HeadInfo head : heads){
			if(head.getId() == id){
				head.markHaved();  //标记已拥有
				break;
			}
		}
		if(updateHandler != null){  //更新ui
			Message msg = updateHandler.obtainMessage(updateHandlerMsgWhat);
			msg.obj = AppConfig.mContext.getString(R.string.head_buy_success);
			msg.sendToTarget();
		}
	}
	
	/**
	 * 购买失败
	 * @param id
	 * @param failCode 错误码
	 * @param info 失败信息
	 */
	public void buyHeadFail(int id, int failCode, String info){
		time = 0;   //重设计时器
		if(failCode == 35){ //余额不足
			for(HeadInfo head : heads){
				if(head.getId() == id){
					short currencyType = head.getCurrencyType();
					if(currencyType == 3 || currencyType == 4){     //金币或乐豆购买的
						if(mAct != null){
							UtilHelper.showBuyDialog(mAct,AppConfig.propId,false,AppConfig.isConfirmon);
						}
						return;
					}
					break;
				}
			}
		}
		if(updateHandler != null){  //更新ui
			Message msg = updateHandler.obtainMessage(updateHandlerMsgWhat);
//			msg.obj = AppConfig.mContext.getString(R.string.head_buy_fail);
			msg.obj = info;
			msg.sendToTarget();
		}
	}
	
	/**
	 * 请求使用id
	 * @param id
	 */
	public void requestUseHead(int id){
		//点击至少间隔2秒
		if(needWait()){
			if(mAct != null){
				Toast.makeText(mAct, mAct.getText(R.string.head_please_wait), Toast.LENGTH_SHORT).show();
			}
			return;
		}
		time = System.currentTimeMillis();    //请求时间
		if(id == curId){   //是当前使用id
			return;
		}
		
		askUseId = id;
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(HallDataManager.getInstance().getUserMe().userID);
		tdos.writeInt(AppConfig.gameId);
		tdos.writeInt(id);
		NetSocketPak pointBalance = new NetSocketPak(LS_MDM_PROP, LSUB_CMD_MODIFY_USER_HEAD_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_MDM_PROP, LSUB_CMD_MODIFY_USER_HEAD_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					int rlt = tdis.readShort();    //0表示成功，其他表示失败
					String msg = tdis.readUTFShort();
					if(rlt == 0){
						curId = askUseId;
						HallDataManager.getInstance().setUserHead((short)curId);
					}

					if(updateHandler != null){
						Message message = updateHandler.obtainMessage(updateHandlerMsgWhat);
						message.obj = msg;
						updateHandler.sendMessage(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				time = 0;     //重设计时器
				return true;
			}
		};

		nPListener.setOnlyRun(false);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();
	}
	
	private static long time = 0;
	/**
	 * 是否需要等待，购买和使用头像时，限制频繁请求
	 * @return
	 */
	private boolean needWait(){
		long newtime = System.currentTimeMillis();
		if(newtime - time < 2000){
			return true;
		}
		return false;
	}
	
	/**
	 * 请求购买头像
	 * @param item
	 */
	public void requestBuyHead(HeadInfo item){
		//点击至少间隔2秒
		if(needWait()){
			if(mAct != null){
				Toast.makeText(mAct, mAct.getText(R.string.head_please_wait), Toast.LENGTH_SHORT).show();
			}
			return;
		}
		time = System.currentTimeMillis();      //请求时间
		if(mAct != null){
			PayManager.getInstance(mAct).requestBuyHead(item);
		}
	}
}

