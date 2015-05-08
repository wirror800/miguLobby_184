package com.mykj.andr.task;

import android.content.res.Resources;

import com.mykj.comm.io.TDataInputStream;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
/***
 * 
 * @ClassName: TaskGiftItemInfo
 * @Description:  任务物品信息：类型、ID、属性、数量
 * @author zhanghuadong
 * @date 2012-9-17 下午02:16:50
 *
 */
public class TaskGiftInfo{
	/**
	WORD		wType			//物品类型代表各种可能赠送的东西
	DWORD		dwGiftID; 		//物品ID为此类型中确定的物品
	DWORD		dwAttribute;	//物品属性代表此物品增加的某种属性，比如数量，时间等
	DWORD		dwValue;		//物品值为具体的增加额度
	 */

	/**
	 任务物品的类型
	enum enGiftType
	{
		emGiftType_Gold		= 1,		//金币
		emGiftType_Bean     = 2,		//豆
		emGiftType_Prop		= 3,		//道具
		emGiftType_Special	= 4,		//特殊物品,比如活动物品
	};
	 */
	private short Type;
	private int GiftID;
	private int Attribute;
	private int Value;



	//任务礼品
	public TaskGiftInfo(TDataInputStream dis){
		if(dis==null){
			return;
		}
		dis.setFront(false);
		Type = dis.readShort();   //
		GiftID = dis.readInt();
		Attribute = dis.readInt();
		Value = dis.readInt();    //
	}

	public TaskGiftInfo(byte[] array){
		this(new TDataInputStream(array));
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		Resources resource = AppConfig.mContext.getResources();
		switch(Type){
		case 1:
			sb.append(Value);
			sb.append(resource.getString(R.string.task_goal));
			sb.append("  ");
			break;
		case 2:
			sb.append(Value);
			sb.append(AppConfig.UNIT);
			sb.append("  ");
			break;
		case 3:
			sb.append(resource.getString(R.string.task_stage_property));
			sb.append(Value);
			sb.append("  ");
			break;
		case 4:
			sb.append(resource.getString(R.string.task_special_goods));
			sb.append(Value);
			sb.append("  ");
			break;
		}
		return sb.toString();
	}

	public short getType() {
		return Type;
	}

	public int getGiftID() {
		return GiftID;
	}

	public int getAttribute() {
		return Attribute;
	}

	public int getValue() {
		return Value;
	}

}
