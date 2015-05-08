package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;


/****************
 * 
 * @ClassName: ChallengeInviteInfo
 * @Description: 约战区邀请返回信息
 * @author zhanghuadong
 * @date 2012-9-27 下午02:51:32
 *
 */
public class ChallengeInviteInfo implements Serializable {
	/**
	 * @Fields serialVersionUID: TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;

	
	
	public int currentRoomUserID;// 房主用户ID
	public String currentChallengeCodeText;// 约战邀请码
	public long currentChallengeCode;      //将邀请码字符串表示形式转换为long型
	
	public short maxpers = 0;     //要求总人数
	public short currentper=0;   // 当前人数
	public int residueTime;      // 邀请码剩余有效时间（需要自己倒计时）
	
	
	public ChallengeInviteInfo(){}
	public ChallengeInviteInfo(TDataInputStream tdis){
		if (tdis == null) {
			return;
		}
		currentRoomUserID = tdis.readInt();        // 房主用户ID
		
		currentChallengeCodeText = tdis.readUTF(8);// 约战邀请码
		currentChallengeCode = getChallengeCode(currentChallengeCodeText);
		
		maxpers = tdis.readShort();// 要求总人数
		currentper = tdis.readShort();// 当前人数
		residueTime = tdis.readInt(); // 邀请码剩余有效时间（需要自己倒计时）
	}
	
	/**
	 * @Title: getChallengeCode
	 * @Description: 将邀请码字符串表示形式转换为long型
	 * @param challCode
	 * @return
	 * @version: 2012-3-7 上午09:55:34
	 */
	private long getChallengeCode(final String challCode){
		if(challCode == null){
			return 0;
		}
		byte bytes[] = challCode.getBytes(); 
		long challengeCode = TDataInputStream.getLongByBytes(bytes, false);
		return challengeCode;
	}
	
}
