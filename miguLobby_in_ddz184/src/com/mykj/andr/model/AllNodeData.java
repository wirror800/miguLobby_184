package com.mykj.andr.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.mykj.andr.ui.widget.CardZoneDataListener;
import com.mykj.comm.io.TDataInputStream;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.Util;


public class AllNodeData {

	public static final String CARD_ZOOM_FILE="cardZone.cfg";

	private static AllNodeData instance = null;
	private Context mContext;
	private List<NodeData> mNodeDataList;
	private List<TDataInputStream> mTdis;
	private List<NodeData> mFirstList;
	private int mRootID=0;

	private int mTotal=0;  //静态数据
	private int mCurCount=0;

	private int mSubCurCount=0;//动态数据

	private String mVersion="";//32 字节,默认为空字符串

	private byte mFirstPager;   //第一页显示方式

	private byte mServerPlayId;   //默认玩法ID

	private boolean isRecFinish=false;

	private boolean isSubRecFinish=false;


	public static int NODE_DATA_ERROR=-1;
	public static int NODE_DATA_SOCKET=1;
	public static int NODE_DATA_LOCAL=2;

	public static final byte JINGDAN_PLAYED=0;  //经典玩法
	public static final byte XIAOBING_PLAYED=1; //小兵玩法
	public static final byte LAIZI_PLAYED=2;    //癞子玩法

	public static final byte JINGDAN=0x01;
	public static final byte XIAOBING=0x02;
	public static final byte LAIZI=0x04;
	public static final byte PLAY_SWITCH=0x08;

	/**
	 * 私有构造函数
	 * @param context
	 */
	private AllNodeData(Context context) {
		mContext = context;

		mNodeDataList=new ArrayList<NodeData>();
		mTdis=new ArrayList<TDataInputStream>();
		mFirstList = new ArrayList<NodeData>();

	}

	/**
	 * 单例
	 * @param context
	 * @return
	 */
	public static AllNodeData getInstance(Context context) {
		if (instance == null) {
			instance = new AllNodeData(context);
		}
		return instance;
	}

	/**
	 * 从本地缓存读取数据不成功，需要清除数据
	 */
	public void cleanNodeData(){
		synchronized (mNodeDataList) {
			mNodeDataList.clear();
		}
		synchronized (mTdis) {
			mTdis.clear();
		}
		synchronized (mFirstList) {
			mFirstList.clear();
		}
		mTotal=0;  //静态数据
		mCurCount=0;
		mSubCurCount=0;//动态数据
	}


	/**
	 * 分区列表数据协议是否接受完成
	 * @return
	 */
	public boolean isRecFinish(){
		return isRecFinish;
	}

	/**
	 * 判断子节点数据更新是否完成
	 * @param nodeid
	 * @return
	 */
	public boolean isSubRecFinish(){
		return isSubRecFinish;
	}


	public List<TDataInputStream> getTdis() {
		if(isRecFinish){
			return mTdis;
		}else{
			return null;
		}
	}

	public int getTotal() {
		return mTotal;	
	}

	public int getCurCount() {
		return mCurCount;
	}

	public String getVersion() {
		return Util.getStringSharedPreferences(mContext, "version", "");
	}



	public int getRootID() {
		return mRootID;
	}

	/**
	 * 获取是否显示推荐卡片
	 * @return
	 */
	public boolean isShowCard(){
		boolean res=false;
		if(mFirstPager==1){
			res=true;
		}
		return res;
	}



	public void clearLocalData(){
		Util.setStringSharedPreferences(mContext, "version", "");
		File file=mContext.getFileStreamPath(CARD_ZOOM_FILE);
		if(file.exists()){
			mContext.deleteFile(CARD_ZOOM_FILE);
		}
	}


	public void saveNodeDataToFile(){
		if(isRecFinish){
			File file=mContext.getFileStreamPath(CARD_ZOOM_FILE);
			if(file.exists()){
				mContext.deleteFile(CARD_ZOOM_FILE);
			}
			int sum=0;
			int copy=0;
			for(int i=0;i<mTdis.size();i++){
				TDataInputStream tdis=mTdis.get(i);
				tdis.reset();
				if(i==0){
					byte[] b=tdis.readBytes();
					sum+=b.length;
				}else{	
					tdis.readShort();
					tdis.readShort();
					tdis.readUTF(32);
					tdis.readInt();
					tdis.readByte();

					switch(CardZoneDataListener.NODE_DATA_PROTOCOL_VER){
					case CardZoneDataListener.VERSION_1://列表协议第一版，每个节点单独请求
						break;
					case CardZoneDataListener.VERSION_2://列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
						break;
					case CardZoneDataListener.VERSION_3://列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
						tdis.readByte();   //玩法选择
						break;
					default:
						break;
					}

					byte[] b=tdis.readBytes();
					sum+=b.length;
				}
			}

			byte[] target = new byte[sum];
			for(int i=0;i<mTdis.size();i++){
				TDataInputStream tdis=mTdis.get(i);
				tdis.reset();
				if(i==0){
					byte[] b=tdis.readBytes();
					if(b.length>4){
						b[2]=b[0];
						b[3]=b[1];
					}

					System.arraycopy(b, 0, target, copy, b.length);
					copy+=b.length;
				}else{					
					tdis.readShort();
					tdis.readShort();
					tdis.readUTF(32);
					tdis.readInt();
					tdis.readByte();
					switch(CardZoneDataListener.NODE_DATA_PROTOCOL_VER){
					case CardZoneDataListener.VERSION_1://列表协议第一版，每个节点单独请求
						break;
					case CardZoneDataListener.VERSION_2://列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
						break;
					case CardZoneDataListener.VERSION_3://列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
						tdis.readByte();   //玩法选择
						break;
					default:
						break;
					}
					byte[] b=tdis.readBytes();
					System.arraycopy(b, 0, target, copy, b.length);
					copy+=b.length;
				}
			}
			Util.saveToFile(file,target);
			if(!Util.isEmptyStr(mVersion)){
				Util.setStringSharedPreferences(mContext, "version", mVersion);
			}
		}
	}

	public  TDataInputStream getNodeDataFromFile(){
		TDataInputStream tdis=null;
		File file=mContext.getFileStreamPath(CARD_ZOOM_FILE);
		if(file.exists()){
			byte[] b=Util.readBytesFromFile(file);	
			tdis=new TDataInputStream(b,false);
		}
		return tdis;
	}


	public NodeData findNodeDataById(int nodeId){
		NodeData nd=null;
		if(isRecFinish){
			synchronized (mNodeDataList) {
				for(NodeData node:mNodeDataList){
					int id=node.ID;
					if(id==nodeId){
						nd=node;
					}
				}
			}
		}
		return nd;
	}



	/**
	 * 
	 * @param tdis
	 * @return -1  数据错误
	 *          1 网络获取数据
	 *          2 本地缓存获取数据
	 */
	public int addTDataInputStream(TDataInputStream tdis){
		if(tdis==null){
			return NODE_DATA_ERROR;
		}
		mTdis.add(tdis);		
		mTotal=tdis.readShort();
		if(mTotal==0){
			TDataInputStream local_tdis=getNodeDataFromFile();
			addLocalTDataInputStream(local_tdis);
			return NODE_DATA_LOCAL;
		}

		int count=tdis.readShort();
		mCurCount+=count;

		mVersion=tdis.readUTF(32);
		mRootID=tdis.readInt();

		mFirstPager=tdis.readByte();
		switch(CardZoneDataListener.NODE_DATA_PROTOCOL_VER){
		case CardZoneDataListener.VERSION_1://列表协议第一版，每个节点单独请求
			break;
		case CardZoneDataListener.VERSION_2://列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
			break;
		case CardZoneDataListener.VERSION_3://列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
			mServerPlayId=tdis.readByte();  //默认玩法ID
			break;
		default:
			break;
		}
		synchronized (mNodeDataList) {
			for (int i = 0; i < count; i++) {
				NodeData nodedata = new NodeData(tdis);
				mNodeDataList.add(nodedata);
			}
		}
		if(mTotal==mCurCount){
			isRecFinish=true;
		}
		return NODE_DATA_SOCKET;
	}



	private void addLocalTDataInputStream(TDataInputStream tdis){
		if(tdis==null){
			return;
		}
		mTotal=tdis.readShort();	
		mCurCount=tdis.readShort();
		mVersion=tdis.readUTF(32);
		mRootID=tdis.readInt();
		mFirstPager=tdis.readByte();

		switch(CardZoneDataListener.NODE_DATA_PROTOCOL_VER){
		case CardZoneDataListener.VERSION_1://列表协议第一版，每个节点单独请求
			break;
		case CardZoneDataListener.VERSION_2://列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
			break;
		case CardZoneDataListener.VERSION_3://列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
			mServerPlayId=tdis.readByte();  //默认玩法ID
			break;
		default:
			break;
		}
		synchronized (mNodeDataList) {
			for (int i = 0; i < mCurCount; i++) {
				NodeData nodedata = new NodeData(tdis);
				mNodeDataList.add(nodedata);
			}
		}
		if(mTotal==mCurCount){
			isRecFinish=true;
		}
	}



	public void updateRoomData(TDataInputStream tdis){
		if(tdis==null){
			return;
		}
		int total=tdis.readShort();
		if(total==0){
			//isSubRecFinish=false;
			return;
		}
		int curCount=tdis.readShort();
		mSubCurCount+=curCount;

		tdis.readInt();  //int parentId=tdis.readInt() 节点父ID,未使用
		for(int i=0;i<curCount;i++){
			SubNodeData sn=new SubNodeData(tdis);
			/*以下为更新房间数据*/
			NodeData node=null;
			int subid=sn.getID();
			int num=sn.getPersons();
			String name=sn.getName();
			node=findNodeDataById(subid);
			if(node!=null){
				node.onLineUser=num;
				node.Name=name;
			}

		}
		if(total==mSubCurCount){
			isSubRecFinish=true;
		}else{
			isSubRecFinish=false;
		}
	}


	/**
	 * 获取分区列表数据
	 * @return
	 */
	public List<NodeData> getAllNodeDate(){
		if(isRecFinish){
			return mNodeDataList;
		}else{
			return null;
		}
	}




	public byte getPlayId() {
		byte play=JINGDAN_PLAYED;//异常情况，默认经典

		if((mServerPlayId & JINGDAN)!=0){
			play=JINGDAN_PLAYED;
		}else if((mServerPlayId & XIAOBING)!=0){
			play=XIAOBING_PLAYED;
		}else if((mServerPlayId & LAIZI)!=0){
			play=LAIZI_PLAYED;
		}

		return play;
	}


	public boolean getPlayedSwitch(){
		boolean res=false;
		if((mServerPlayId & PLAY_SWITCH)!=0){
			res=true;
		}
		return res;
	}



	//******************************以下1.5.0-1.5.2 列表数据*******************************************
	/**
	 * 获取抽屉一级节点
	 */
	public List<NodeData> getDropNodeDate(){
		if(isRecFinish){
			List<NodeData> firstDataList=new ArrayList<NodeData>();
			NodeData rootNode=new NodeData();
			rootNode.ID=mRootID;
			rootNode.Name=mContext.getResources().getString(R.string.recommend);
			firstDataList.add(rootNode);
			firstDataList.addAll(getFirstNodeDate());
			return firstDataList;
		}else{
			return null;
		}
	}



	/**
	 * 获取分区一级节点
	 */
	public List<NodeData> getFirstNodeDate(){
		return getFirstNodeDate(false);
	}

	private List<NodeData> getFirstNodeDate(boolean needUpdate){
		if(isRecFinish){
			if(needUpdate || mFirstList.isEmpty()){
				if(!mFirstList.isEmpty()){
					mFirstList.clear();
				}
				synchronized (mNodeDataList) {
					for(NodeData node:mNodeDataList){
						if(node.ParentID==mRootID
								&&node.Type!=NodeData.NODE_MM_VIDEO){
							mFirstList.add(node);
						}
					}
				}
			}
		}else{
			if(!mFirstList.isEmpty()){
				mFirstList.clear();
			}
		}
		return mFirstList;
	}



	/**
	 * 获取一级节点的二级子节点列表
	 * @param parentId
	 * @return
	 */
	public  List<NodeData> getSecondNodeDate(final int parentId){
		if(isRecFinish){
			List<NodeData> SecondDataList=new ArrayList<NodeData>();
			synchronized (mNodeDataList) {
				for(NodeData subnode : mNodeDataList){
					if(parentId==subnode.ParentID){
						SecondDataList.add(subnode);
					}
				}
			}
			return SecondDataList;
		}else{
			return null;
		}

	}




	/**
	 * 获取卡片分区根节点
	 * @return
	 */
	public NodeData getCardNode(){
		if(isRecFinish){
			NodeData rootNode = new NodeData();
			rootNode.ID = mRootID;
			rootNode.Name=mContext.getResources().getString(R.string.recommend);
			return rootNode;
		}else{
			return null;
		}
	}





	/**
	 * 获取快速开始房间节点
	 * 只返回一个节点
	 * @return
	 */
	private NodeData getQuickEntryNodeDate(){
		NodeData node=null;
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();

		int myBean = userInfo.bean;
		List<NodeData> entryList = findNodeDataByBean(myBean);
		if(entryList.size()>=1){
			node=entryList.get(0);
		}
		if(node==null){
			List<NodeData> allNode=getAllNodeDate();
			for(NodeData item:allNode){
				if(item.ParentID!=mRootID && item.Recommend!=1){
					node=item;
					break;
				}
			}
		}

		return node;
	}



	/**
	 * 获取推荐节点
	 * 推荐节点只有一个
	 * @return
	 */

	private NodeData getRecommendNodeDate(NodeData nd){
		NodeData node=null;
		List<NodeData> list=new ArrayList<NodeData>();
		if(isRecFinish){
			synchronized (mNodeDataList) {
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.Recommend==1 && nodeData.ParentID!=mRootID){
						list.add(nodeData);
					}
				}
			}
		}
		if(list.size()==0){
			synchronized (mNodeDataList) {
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID){
						list.add(nodeData);
					}
				}
			}
		}
		for(NodeData item:list){
			if(item.ID!=nd.ID){
				node=item;
				break;
			}
		}
		if(node==null){
			synchronized (mNodeDataList) {
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID){
						node= nodeData;
						break;
					}
				}
			}
		}

		if(node==null){   //实在找不到推荐位，使用第一个卡片快速开始卡片为推荐位，防止程序崩溃
			node=nd;
		}
		return node;
	}


	/**
	 * 获取第一个分区节点
	 * @return
	 */
	private NodeData getFirstCardNodeData(){
		NodeData node=null;
		List<NodeData> freeMatchNodelists=findFreeMatchData();  //自由节点，城市赛
		List<NodeData> firstNodeList=getFirstNodeDate();

		for(NodeData item:freeMatchNodelists){
			if(item.Recommend==2){
				node=item;
				break;
			}

		}
		if(node==null){
			for(NodeData item:firstNodeList){
				if(item.Recommend==1){
					node=item;
					break;
				}

			}

		}
		if(node==null){
			if(getFirstNodeDate().size()>=1){
				node=getFirstNodeDate().get(0);
			}
		}

		return node;
	}

	/**
	 * 获取所有自由场 城市赛 二级节点
	 * 自由场 城市赛  为一个节点类型
	 * @return
	 */
	private List<NodeData> findFreeMatchData(){
		List<NodeData> list=new ArrayList<NodeData>();
		if(isRecFinish){
			for(NodeData node:mNodeDataList){
				if(node.Type == NodeData.NODE_NORMAL){
					list.add(node);
				}
			}
		}
		return list;
	}




	/**
	 * 获取乐豆要求跟用户乐豆数相符合的房间
	 * @param bean
	 * @return
	 */
	private List<NodeData> findNodeDataByBean(int bean){
		List<NodeData> list=new ArrayList<NodeData>();
		if(isRecFinish){
			synchronized (mNodeDataList) {
				for(NodeData node:mNodeDataList){
					int max = -1;// 乐豆的最大值
					int min = -1;
					if (node.limits != null) {
						for (int k = 0; k < node.limits.length; k++) // 查找该房间是否是乐豆快速房间
						{
							if (node.limits[k].Type == 3) // 找到乐豆类型
							{
								max = node.limits[k].Max; // 找到最大值
								min = node.limits[k].Min; // 找到最小值

								if(bean>=min && bean<=max){
									list.add(node);
								}
							}

						}

					}

				}
			}
		}
		return list;
	}

	//******************************以上1.5.0-1.5.2 列表数据*******************************************





	//*****************************以下1.5.3 三玩法列表数据**********************************************
	/**
	 * 获取分区一级节点
	 */
	public List<NodeData> getFirstNodeDate(byte palyId){
		return getFirstNodeDate(palyId,true);
	}

	private List<NodeData> getFirstNodeDate(byte palyId,boolean needUpdate){
		if(isRecFinish){
			if(needUpdate || mFirstList.isEmpty()){
				if(!mFirstList.isEmpty()){
					mFirstList.clear();
				}
				synchronized (mNodeDataList) {
					for(NodeData node:mNodeDataList){
						if(node.ParentID==mRootID && node.PlayID==palyId){
							mFirstList.add(node);
						}
					}
				}
			}
		}else{
			if(!mFirstList.isEmpty()){
				mFirstList.clear();
			}
		}
		return mFirstList;
	}


	/**
	 * 获取一级节点的二级子节点列表
	 * @param parentId
	 * @return
	 */
	public  List<NodeData> getSecondNodeDate(final int parentId,final int playId){
		if(isRecFinish){
			List<NodeData> SecondDataList=new ArrayList<NodeData>();
			synchronized (mNodeDataList) {
				for(NodeData subnode : mNodeDataList){
					if(parentId==subnode.ParentID && playId==subnode.PlayID){
						SecondDataList.add(subnode);
					}
				}
			}
			return SecondDataList;
		}else{
			return null;
		}

	}


	/**
	 * 获取推荐卡片分区节点
	 * 设定卡片推荐节点必须为三个
	 * 第一个 快速进入房间
	 * 第二个 服务器推荐房间
	 * 第三个 服务器第一个分区（第一个一级节点）
	 * @return
	 */
	public List<NodeData> getCardZoneNodeDate(final byte playId){
		List<NodeData> cardZoneList=new ArrayList<NodeData>();
		if(isRecFinish){
			//自由场节点
			cardZoneList.add(getFirstCard(playId));

			//比赛场节点
			NodeData sc=getSecondCard(playId);

			if(sc!=null){
				cardZoneList.add(sc);
			}

			//美女玩法节点
			NodeData nd=getMMVideoNodeData(playId);  //获取美女视频节点，不区分玩法
			cardZoneList.add(nd);
			
			return cardZoneList;
		}

		return null;
	}


	/**
	 * 获取推荐卡片分区节点
	 * 设定卡片推荐节点必须为三个
	 * 第一个 快速进入房间
	 * 第二个 服务器推荐房间
	 * 第三个 服务器第一个分区（第一个一级节点）
	 * @return
	 */
	public List<NodeData> getCardZoneNodeDate(){
		List<NodeData> cardZoneList=new ArrayList<NodeData>();
		if(isRecFinish){
			//添加一级节点
			//cardZoneList.add(getFirstCardNodeData(playId));
			cardZoneList.add(getFirstCard());		
			
			//推荐节点
			cardZoneList.add(getSecondCard());
			
			//快速进入节点
			//NodeData nd=getQuickEntryNodeDate(playId);
			NodeData nd=getMMVideoNodeData((byte)0);  //获取美女视频节点，不区分玩法
			cardZoneList.add(nd);

		}

		if(cardZoneList.size()==3){
			return cardZoneList;
		}else{
			return null;
		}
	}
	

	/**
	 * 获取快速开始房间节点
	 * 只返回一个节点
	 * @return
	 */
	private NodeData getQuickEntryNodeDate(final byte playId){
		NodeData node=null;
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();

		int myBean = userInfo.bean;
		List<NodeData> entryList = findNodeDataByBean(myBean,playId);
		if(entryList.size()>=1){
			node=entryList.get(0);
		}
		if(node==null){
			List<NodeData> allNode=getAllNodeDate();
			for(NodeData item:allNode){
				if(item.ParentID!=mRootID && item.Recommend!=1 && item.PlayID==playId){
					node=item;
					break;
				}
			}
		}

		if(node==null){
			List<NodeData> allNode=getAllNodeDate();
			for(NodeData item:allNode){
				if(item.PlayID==playId){
					node=item;
					break;
				}
			}
		}

		return node;
	}


	/**
	 * 获取推荐节点
	 * 推荐节点只有一个
	 * @return
	 */

	private NodeData getRecommendNodeDate(NodeData nd,final byte playId){
		NodeData node=null;
		List<NodeData> list=new ArrayList<NodeData>();
		synchronized (mNodeDataList) {
			if(isRecFinish){
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.Recommend==1 && nodeData.ParentID!=mRootID && nodeData.PlayID==playId){
						list.add(nodeData);
					}
				}
			}
			if(list.size()==0){
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID && nodeData.PlayID==playId){
						list.add(nodeData);
					}
				}
			}
			for(NodeData item:list){
				if(item.ID!=nd.ID && item.PlayID==playId){
					node=item;
					break;
				}
			}
			if(node==null){
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID && nodeData.PlayID==playId){
						node= nodeData;
						break;
					}
				}
			}

			if(node==null){   //实在找不到推荐位，使用第一个卡片快速开始卡片为推荐位，防止程序崩溃
				node=nd;
			}
		}
		return node;
	}



	/**
	 * 获取第一个分区节点
	 * @return
	 */
	private NodeData getFirstCardNodeData(final byte playId){
		NodeData node=null;
		List<NodeData> freeMatchNodelists=findFreeMatchData(playId);  //自由节点，城市赛
		List<NodeData> firstNodeList=getFirstNodeDate(playId);

		for(NodeData item:freeMatchNodelists){
			if(item.Recommend==2 && item.PlayID==playId){
				node=item;
				break;
			}

		}
		if(node==null){
			for(NodeData item:firstNodeList){
				if(item.Recommend==1){
					node=item;
					break;
				}

			}

		}
		if(node==null){
			if(getFirstNodeDate().size()>=1){
				node=getFirstNodeDate().get(0);
			}
		}

		return node;
	}



	/**
	 * 获取所有自由场 城市赛 二级节点
	 * 自由场 城市赛  为一个节点类型
	 * @return
	 */
	private List<NodeData> findFreeMatchData(final byte playId){
		List<NodeData> list=new ArrayList<NodeData>();
		if(isRecFinish){
			for(NodeData node:mNodeDataList){
				if(node.Type == NodeData.NODE_NORMAL  && node.PlayID==playId){
					list.add(node);
				}
			}
		}
		return list;
	}




	/**
	 * 获取乐豆要求跟用户乐豆数相符合的房间
	 * @param bean
	 * @return
	 */
	private List<NodeData> findNodeDataByBean(int bean,final byte playId){
		List<NodeData> list=new ArrayList<NodeData>();
		if(isRecFinish){
			synchronized (mNodeDataList) {
				for(NodeData node:mNodeDataList){
					int max = -1;// 乐豆的最大值
					int min = -1;
					if (node.limits != null && node.PlayID==playId) {
						for (int k = 0; k < node.limits.length; k++) // 查找该房间是否是乐豆快速房间
						{
							if (node.limits[k].Type == 3) // 找到乐豆类型
							{
								max = node.limits[k].Max; // 找到最大值
								min = node.limits[k].Min; // 找到最小值

								if(bean>=min && bean<=max){
									list.add(node);
								}
							}

						}

					}
				}
			}
		}
		return list;
	}
	//*****************************以上1.5.3 三玩法列表数据**********************************************

	/**
	 * 通过节点ID获取nodedata
	 * @param nodeId
	 * @return
	 */
	public NodeData getNodeDataById(int nodeId){
		NodeData nodeData=null;
		if(isRecFinish){
			for(NodeData node:mNodeDataList){
				if(node.ID==nodeId){
					nodeData=node;
					break;
				}
			}

		}
		return nodeData;
	}


	/**
	 * 通过节点dataId获取nodedata
	 * @param nodeId
	 * @return
	 */
	public NodeData getNodeDataByDataId(long dataId){
		NodeData nodeData=null;
		if(isRecFinish){
			for(NodeData node:mNodeDataList){
				if(node.dataID==dataId){
					nodeData=node;
					break;
				}
			}

		}
		return nodeData;
	}

	
	/**
	 * 获取推荐节点
	 * 推荐节点只有一个
	 * @return
	 */

	private NodeData getRecommendNodeDate(){
		NodeData node=null;
		List<NodeData> list=new ArrayList<NodeData>();
		if(isRecFinish){
			synchronized (mNodeDataList) {
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.Recommend==1 && nodeData.ParentID!=mRootID){
						list.add(nodeData);
					}
				}
			}
		}
		if(list.size()==0){
			synchronized (mNodeDataList) {
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID){
						list.add(nodeData);
					}
				}
			}
		}
		for(NodeData item:list){
			node=item;
			break;
		}
		if(node==null){
			synchronized (mNodeDataList) {
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID){
						node= nodeData;
						break;
					}
				}
			}
		}

		return node;
	}
	
	
	private NodeData getRecommendNodeDate(final byte playId){
		NodeData node=null;
		List<NodeData> list=new ArrayList<NodeData>();
		synchronized (mNodeDataList) {
			if(isRecFinish){
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.Recommend==1 && nodeData.ParentID!=mRootID && nodeData.PlayID==playId){
						list.add(nodeData);
					}
				}
			}
			if(list.size()==0){
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID && nodeData.PlayID==playId){
						list.add(nodeData);
					}
				}
			}
			for(NodeData item:list){
				if(item.PlayID==playId){
					node=item;
					break;
				}
			}
			if(node==null){
				for(NodeData nodeData:mNodeDataList){
					if(nodeData.IconID!=0 && nodeData.ParentID!=mRootID && nodeData.PlayID==playId){
						node= nodeData;
						break;
					}
				}
			}
		}
		return node;
	}
	
	
	//*****************************1.8.0 美女视频新增**********************************************
	
	
	
	/**
	 * 获取第一个卡片节点<现写死固定自由场>
	 * @param palyId
	 * @return
	 */
	private NodeData getFirstCard(final byte playId){
		NodeData node=null;
		List<NodeData> list=getFirstNodeDate(playId);
		for(NodeData item:list){
			if(item.Type == NodeData.NODE_NORMAL
					&& item.PlayID==playId){
				node=item;
				break;
			}
		}
		
		if(node==null){
			node=getRecommendNodeDate(playId);
		}
		
		return node;
	}
	
	
	/**
	 * 获取第二个卡片节点<现写死固定比赛场>
	 * @param palyId
	 * @return
	 */
	private NodeData getSecondCard(final byte playId){

		NodeData node=null;
		List<NodeData> list=getFirstNodeDate(playId);
		for(NodeData item:list){
			if(item.Type == NodeData.NODE_ENROLL
					&& item.PlayID==playId){
				node=item;
				break;
			}
		}

		return node;

	}
	
	
	
	
	/**
	 * 获取第一个卡片节点<现写死固定自由场>
	 * @param palyId
	 * @return
	 */
	private NodeData getFirstCard(){
		NodeData node=null;
		List<NodeData> list=getFirstNodeDate();
		for(NodeData item:list){
			if(item.Type == NodeData.NODE_NORMAL){
				node=item;
				break;
			}
		}
		
		if(node==null){
			//node=getRecommendNodeDate(palyId);
		}
		
		return node;
	}
	
	
	
	/**
	 * 获取第二个卡片节点<现写死固定比赛场>
	 * @param palyId
	 * @return
	 */
	private NodeData getSecondCard(){

		NodeData node=null;
		List<NodeData> list=getFirstNodeDate();
		for(NodeData item:list){
			if(item.Type == NodeData.NODE_ENROLL){
				node=item;
				break;
			}
		}
		
		
		return node;
	
	}
	
	
	/**
     * 获取第三个卡片节点<现写死固定美女房间>
	 * 获取美女视频节点，不区分玩法
	 * 只返回一个节点
	 * @return
	 */
	private NodeData getMMVideoNodeData(final byte playId){
		NodeData node=null;
			List<NodeData> allNode=getAllNodeDate();
			for(NodeData item:allNode){
				if(item.ParentID!=mRootID 
						&& item.NewRecommendFlag==1
						&& item.NewRecommendVisible==1){
					node=item;
					break;
				}
			}
		node=null;//强行关闭美女视频入口 @chenqy 2015.4.30	
			
		if(node==null){
			node=getRecommendNodeDate(playId); //获取推荐节点
		}
		
		if(node==null){  //获取快速开始节点
			UserInfo userInfo = HallDataManager.getInstance().getUserMe();
			int myBean = userInfo.bean;
			List<NodeData> entryList = findNodeDataByBean(myBean);
			if(entryList.size()>=1){
				node=entryList.get(0);
			}
		}
		
		return node;
		
	}

	
}
