package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;

/**
 * 
 * @ClassName: VipData
 * @Description: person vip message
 * @author  
 * @date 2013-6-8 下午05:22:54
 *
 */
public class VipData implements Serializable {
	/**
	 * @Fields serialVersionUID: TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;

	public short wType;        //VIP类型
	public short wLevel;			  //等级
	public int  dwGroup;     //成长值
	public short dwNextGroup;  //下一级成长值（0则表示已经最高级）
	public String cOutTimer;    //过期时间
	public byte	 cbIsCanAdward;  //是否可以领奖
	 
	
	
	
	public VipData(TDataInputStream dis){
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		final int len=dis.readShort();
		MDataMark mark=dis.markData(len);
		
		wType=dis.readShort();
		wLevel=dis.readShort();
		dwGroup = dis.readInt(); // 节点标识
		dwNextGroup=dis.readShort();
		cOutTimer=dis.readUTF(dis.readByte()); //房间名称
		cbIsCanAdward=dis.readByte();
		dis.unMark(mark);
	}
}
