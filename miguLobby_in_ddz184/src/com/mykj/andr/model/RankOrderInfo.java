package com.mykj.andr.model;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;

/***
 * 
 * @ClassName: RankOrderInfo
 * @Description: 比赛排名信息
 * @author  
 * @date 2013-4-23 下午06:23:00
 *
 */
public class RankOrderInfo {

	/**
	 * WORD wMatchLen; //后接数据长度<BR>
	 * WORD wOrder; //名次<BR>
	 * DWORD dwUserID; //用户编号<BR>
	 * __int64 iScore; //用户积分<BR>
	 * DWORD dwRoundCount;//总局数<BR>
	 * DWORD dwWinCount;//胜局数<BR>
	 * BYTE cbNickNameLen;//昵称长度<BR>
	 * char szNickName[1];//昵称,手机Utf-8<BR>
	 **/
	public short order = -1;
	public int userID = -1;

	public byte	cbPromotionFlag;
	public int score = 0;

	public int roundCount = 0;
	public int winCount = 0;
	public String nickName = "";

	public RankOrderInfo(TDataInputStream dis){

		final int len=dis.readShort();          // 此数据块长度
		MDataMark mark=dis.markData(len);

		userID = dis.readInt();// 用户编号	
		order = dis.readShort(); // 名次
		cbPromotionFlag=dis.readByte();
		score = dis.readInt();// 用户积分

		byte length=dis.readByte();
		nickName = dis.readUTF(length);

		dis.unMark(mark);
	}

	/**
	 * @Title: getWinRatio
	 * @Description: 获得当前胜率
	 * @return
	 * @version: 2011-12-20 下午02:34:51
	 */
	public int getWinRatio(){
		if(roundCount == 0){
			return 0;
		}
		int winc = winCount * 100;
		int roundc = roundCount;
		return winc / roundc;
	}



}
