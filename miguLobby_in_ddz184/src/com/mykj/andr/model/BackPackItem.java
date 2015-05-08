package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;
/***********
 * 背包中我的物品
 * @author  
 *
 */
public class BackPackItem implements Serializable {

	/**
	 * 默认折叠，图标向下，同时不显示子布局控件
	 */
	public boolean isArrowUp=false;
	
	private static final long serialVersionUID = 1L;
	public long IndexID;    //道具索引编号,默认为0（时间道具不为0，为后续扩展使用）
	public int id;        //道具ID
	public int PTypeID;  //背包父节点编号

	/**
	 * 道具属性, 现使用Attribute[0]的前3位，第一位表示大厅是否可用0不可用，1可用
	 * 第二位表示房间是否可用，第三位表示桌子是否可用。如001xxxxxxxxxxxxx表示只在桌子上可用
	 */
	public byte[] Attribute = new byte[4];
	public String ExpireTime;  //到期时间 如： 1111201325 (2011年11月20日13点25分到期)
	public int pointValue; //道具点数
	
	public short HoldCount;//用户持有的数量 
	public String backpackName;  //名称数据
	public String backpackPhoto="";  //图片文件名 
    public String backpackDescrip="";  //商品详细描述
    public short urlId;//道具跳转wap的url
    public int newHoldCount;//用户持有的数量  新 修改数据类型为Int
	
	public BackPackItem(TDataInputStream dis){
		if(dis==null){
			return;
		}
		//int dataLen=dis.readShort(); // 此数据块长度 
		//dis.markLen(dataLen);
		
		final int len=dis.readShort();
		MDataMark mark=dis.markData(len);
		
		
		IndexID=dis.readLong(); //道具索引编号,默认为0（时间道具不为0，为后续扩展使用）
		id=dis.readInt();  //道具ID
		PTypeID=dis.readInt();  //背包父节点编号
		dis.read(Attribute, 0, 4); //道具属性, Attribute二进制位
		ExpireTime=dis.readUTF(10);
		pointValue=dis.readInt();  // 道具点数
		HoldCount=dis.readShort();  //用户持有的数量  
		backpackName=dis.readUTFByte();  //名称数据
		backpackPhoto=dis.readUTFByte(); //图片文件名
		backpackDescrip=dis.readUTFShort(); // 商品描述 
		urlId = dis.readShort();
		newHoldCount = dis.readInt();
		//dis.unMark();
		dis.unMark(mark);
	}
	
	public BackPackItem(byte[] array){
		this(new TDataInputStream(array));
	}

}
