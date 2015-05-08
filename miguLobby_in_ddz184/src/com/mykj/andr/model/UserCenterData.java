package com.mykj.andr.model;

import com.mykj.comm.io.TDataInputStream;

public class UserCenterData {
	/** 账号 */
	public String account;
	/** 昵称 */
	public String nickName;
	/** ID */
	public long ID;
	/** 性别 女 0 男1 */
	public byte sex;
	/** 乐豆 */
	public int leDou;
	/** 周积分 */
	public int zhoujifen;
	/** 累积积分 */
	public int leiJiJiFen;
	/** 周排名 */
	public int zhoupaiming;
	/** 元宝 */
	public int yuanbao;
	/** 话费券 */
	public int huafeiquan;
	/** 话费券道具ID */
	public int huafeiquanPropId;
	/** 元宝兑换URL */
	public short duihuanurlid;
	/** 周排名详细URL */
	public short paimingurlid;
	/** 信息修改跳转ID */
	public short xiugaiurlid;
	/** 更多详细排名URL */
	public short morepaimingurlid;

	public int masterScore;
	public short masterHelpID;

	public UserCenterData(){}

	public UserCenterData(byte[] array){
		this(new TDataInputStream(array));
	}

	public UserCenterData(TDataInputStream tdis){
		if (tdis == null) {
			return;
		}
		tdis.setFront(false);
		account = tdis.readUTFByte();
		nickName = tdis.readUTFByte();
		ID = tdis.readLong();
		sex = tdis.readByte();
		leDou = tdis.readInt();
		zhoujifen = tdis.readInt();
		leiJiJiFen = tdis.readInt();
		zhoupaiming = tdis.readInt();
		yuanbao = tdis.readInt();
		huafeiquan = tdis.readInt();
		huafeiquanPropId = tdis.readInt();
		duihuanurlid = tdis.readShort();
		paimingurlid = tdis.readShort();
		xiugaiurlid = tdis.readShort();
		short extendDataLen = tdis.readShort();
		if (extendDataLen > 0) {
			// 读取扩展数据
			morepaimingurlid = tdis.readShort();
			// 2013-1-22日后新增(大师分)
			masterScore = tdis.readInt(); // 大师分
			masterHelpID = tdis.readShort(); // 更多详细排名URL
		}
	}

	
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public byte getSex() {
		return sex;
	}

	public void setSex(byte sex) {
		this.sex = sex;
	}

	public int getLeDou() {
		return leDou;
	}

	public void setLeDou(int leDou) {
		this.leDou = leDou;
	}

	
	
}
